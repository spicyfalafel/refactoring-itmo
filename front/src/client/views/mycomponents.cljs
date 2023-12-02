(ns client.views.mycomponents
  (:require
   [re-frame.core :as re-frame :refer [dispatch subscribe]]
   [client.events.core :as events]
   [client.events.ticket-events :as t]
   [client.events.events-events :as e]
   [client.subs.core :as subs]
   [client.subs.tickets :as tsubs]
   [client.subs.events :as esubs]
   [reagent.core :as r]
   [goog.string :as gstring]
   [goog.string.format]
   [client.views.myclasses :as cls])
  (:require-macros [stylo.core :refer [c]]))

(defn required-* []
  [:span {:style {:color "#dc2626"}} "*"])

(defn paging-label [first-index last-index max-resources]
  [:span {:class (c :font-light) :style {:color "gray"}}
   (gstring/format "%s-%s of %s" (str first-index)
                   (str last-index) (str max-resources))])

(defn selector [selector-values on-change-fn & {cls :cls
                                                default-value :default-value}]
  [:select {:class
            (if cls
              [cls/base-class cls]
              [cls/base-class (c :w-full)])
            :defaultValue (or default-value nil)
            :on-change on-change-fn}
   (when-not default-value
     [:option {:class cls/base-class
               :value (or default-value nil)} "-"])
   (doall (for [value selector-values]
            ^{:key value}
            [:option {:class cls/base-class
                      :value (if
                              (map? value)
                               (:value value)
                               value)}
             (str (if (map? value)
                    (:desc value)
                    value))]))])

(defn modal [heading-label
             modal-content-html
             bottom-html
             & size]
  [:div.darkBG
   [:div.centered
    [:div {:class (if size size :modal-small)}
     [:div.modalHeader
      [:h5.heading heading-label]]
     [:div {:class (if size :modalContent :modalContent-center)}
      modal-content-html]
     [:div.modalActions
      [:div.actionsContainer
       bottom-html]]]]])

(defn delete-icon []
  [:span [:i.fa-solid.fa-trash
          {:class (c [:px 3])}]])

(defn page-circle [value & selected]
  [:div {:class (c [:w 10]
                   :cursor-pointer
                   [:pt 2]
                   [:rounded :xl]
                   [:hover :shadow-inner [:bg :gray-200]]
                   :text-center
                   [:h 10])
         :style (when selected {:background-color "#4281f5"
                                :color "white"})
         :on-click #(dispatch [::events/change-page value])}
   value])

(defn paging-view [events-number]
  (when (> events-number 0)
    (let [current-page @(subscribe [::subs/current-page])
          last-page @(subscribe [::subs/last-page])]
      [:<>
       (when (> (dec current-page) 1) ; first page
         (page-circle 1))
       (when (< 2 current-page) ; ...
         "...")
       (when (>= (dec current-page) 1) ; prev page
         (page-circle (dec current-page)))

       (page-circle current-page true) ; current page

       (when (>= last-page (inc current-page))
         (page-circle (inc current-page))) ; next page

       (when (> (- last-page current-page) 1) ; ... if page is not last or prev-last
         "...")

       (when (> (- last-page current-page) 1)
         (page-circle last-page))])))

(defn input-with-init-value [entity-id
                             prop-path
                             label
                             label-id
                             descr
                             required?
                             & [select-values]]
  (let [mode @(subscribe [::subs/mode])
        entity (if (= :tickets mode)
                 @(subscribe [::tsubs/ticket-by-id entity-id])
                 @(subscribe [::esubs/event-by-id entity-id]))
        the-value (r/atom (get-in entity prop-path))
        event
        (if (= :tickets mode)
          ::t/change-ticket-and-validate
          ::e/change-event-and-validate)
        save-change-fn
        #(dispatch [:dispatch-debounce
                    {:delay 300
                     :event [event prop-path (.. % -target -value)]}])]
    (fn []
      [:div
       {:class (c :border)}
       [:span
        [:label {:for label-id} label
         (when required? [required-*])]]
       [:div {:class (c :text-sm)}
        [:div {:class (c :flex :flex-col)}
         (if select-values
           (selector
            select-values
            save-change-fn
            {:default-value "-"
             :cls (c :w-full [:mb 2])})

           [:input {:name label-id
                    :id label-id
                    :value @the-value
                    :class (c :border [:h 10] :text-2xl)
                    :maxLength 30
                    :type "text"
                    :on-change (fn [value]
                                 (reset! the-value (-> value .-target .-value))
                                 (dispatch [:dispatch-debounce
                                            {:delay 300
                                             :event [event prop-path @the-value]}]))}])

         (when-not select-values
           [:label {:for label-id
                    :class (c :text-lg :font-light :italic)} (when descr descr)])]]])))

(defn paging []
  (let [tickets @(re-frame/subscribe [::tsubs/tickets])
        events @(re-frame/subscribe [::esubs/events])
        page-number @(subscribe [::subs/current-page])
        page-size @(subscribe [::subs/page-size])
        count-tickets @(subscribe [::tsubs/count-tickets])
        count-events @(subscribe [::esubs/count-events])
        mode @(subscribe [::subs/mode])
        entity (if (= mode :tickets) tickets events)
        entity-count (if (= mode :tickets) count-tickets count-events)]
    [:div
     {:class (c :flex :content-between :justify-between [:mb 5] [:mx 10])}
     [:span
      (paging-label
       (inc (* (dec page-number) page-size))
       (+ (* (dec page-number) page-size)
          (count entity))
       entity-count)

      [selector [1 5 10 15 20 30 40 50 60]
       #(dispatch [::events/change-page-size
                   (.. % -target -value)])
       {:default-value 5
        :cls (c [:w 15] [:ml 3])}]]
     [:div {:class (c :flex [:gap 4]
                      :items-center
                      :content-center
                      :justify-center)}
      [paging-view entity-count]]]))
