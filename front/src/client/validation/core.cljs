(ns client.validation.core
  (:require
   [client.validation.ticket :as ticket-validation]
   [client.validation.event :as event-validation]
   [cljs.spec.alpha :as s]))

(defn get-message
  [via messages]
  (->> (reverse via)
       (some messages)))

(defn explain-spec->data [problems messages]
  (mapv
   (fn [{path :path via :via}]
     {:path path
      :message (get-message via messages)})
   problems))

(defn validate-event [new-event]
  (if (s/valid? ::event-validation/event new-event) :ok
    (filter (fn [m] (-> m :path not-empty))
              (explain-spec->data
                (:cljs.spec.alpha/problems (s/explain-data ::event-validation/event new-event))
                event-validation/event-messages))))

(defn validate-ticket [new-ticket]
  (if (s/valid? ::ticket-validation/ticket new-ticket) :ok
    (filter (fn [m] (-> m :path not-empty))
            (explain-spec->data
              (:cljs.spec.alpha/problems  (s/explain-data ::ticket-validation/ticket new-ticket))
              ticket-validation/ticket-messages))))
