(ns client.events.http-events
  (:require [client.config :refer [back-url]]
            [client.debounce]
            [re-frame-cljs-http.http-fx]
            [clojure.string :as str]))

(defn full-url [endpoint]
  (str back-url endpoint))

(defn call-http
  ([url method on-success on-failure]
   {:http-cljs {:method method
                :params {}
                :with-credentials? false
                :on-success on-success
                :on-failure on-failure
                :url url}})
  ([url method on-success on-failure params]
   {:http-cljs (merge {:method method
                       :params {}
                       :format :json
                       :with-credentials? false
                       :on-success on-success
                       :on-failure on-failure
                       :url url}
                      params)}))

(defn http-get [url on-success on-failure]
  (call-http url :get on-success on-failure))

(defn http-delete [url on-success on-failure]
  (call-http url :delete on-success on-failure))

(defn http-post [url body on-success on-failure]
  (call-http url :post on-success on-failure {:params body}))

(defn http-put [db url body on-success on-failure]
  {:db (assoc db :loading true)
   :http-cljs {:method :put
               :params body
               :format :json
               :with-credentials? false
               :on-success on-success
               :on-failure on-failure
               :url url}})


(defn filter-added-filters [db]
  (let [mode (get db :mode)]
    (when mode
      (into {}
            (filter
             (fn [[_field {:keys [value]}]]
               (not= value ""))
             (mode (:filters db)))))))

(defn operator-str->backend-operator [operator]
  (case operator
    ">"  "gt"
    "<"  "lt"
    "="  "eq"
    "!="  "ne"
    nil))

(defn filters-map->str [filters-map]
  (when filters-map
    (->>
      (reduce
        (fn [acc [field {:keys [value operator]}]]
          (str acc "&filter=" (name field)
               "%5B"
               (or (operator-str->backend-operator operator) "eq")
               "%5D%3D" value))
        ""
        filters-map)
      (drop 1)
      (apply str))))

(defn page-url [db]
  (let [current-page (get-in db [:paging :current-page])
        page-size (get-in db [:paging :page-size])]
    (str "offset=" (* (dec current-page) page-size) "&limit=" page-size)))

(defn sortings->str [db part]
  (let [sorting-map
        (get-in db [part :sorting])]
    (str "&sort="
         (str/join ","
                   (mapv (fn [[_id {field :field sort-order :sort-order}]]
                           (str (when (= "desc" sort-order) "-")
                                (name field))) sorting-map)))))



(defn make-url-with-sort-and-filter [db base]
  (let [mode (:mode db)
        part (if (= mode :events) :event :ticket)]
    (cond->
     (str (full-url base) "?" (page-url db))

      (filters-map->str (filter-added-filters db))
      (str "&" (filters-map->str (filter-added-filters db)))

      (get-in db [part :sorting])

      (str (sortings->str db part)))))


