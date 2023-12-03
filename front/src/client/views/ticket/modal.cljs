(ns client.views.ticket.modal
  (:require
   [re-frame.core :as re-frame :refer [dispatch subscribe]]
   [client.events.ticket-events :as t]
   [client.events.common-events :as ce]
   [client.debounce]
   [goog.string :as gstring]
   [goog.string.format]
   [client.views.mycomponents :as components]
   [client.subs.tickets :as tsubs]
   [client.subs.events :as esubs])
  (:require-macros [stylo.core :refer [c]]))

(defn nice-events [events]
  (mapv (fn [[event-id event]]
          {:value (long event-id)
           :desc
           (str
            (or (:name event) "Неизвестное мероприятие")
            " "
            (:date event))})
        events))

(defn ticket-new-prop [prop-path label label-id descr required?
                       & [select-values default-value]]
  ((components/entity-new-prop
    ::tsubs/form-prop
    ::tsubs/form-path-invalid-message
    ::t/change-ticket-and-validate)
   prop-path label label-id descr required? select-values default-value))

(defn new-ticket-top []
  (let
   [events @(subscribe [::esubs/events])
    nice-events (nice-events events)
    ticket-types @(subscribe [::tsubs/ticket-types])]
    [:div
     {:class (c :grid [:grid-cols 2])}
     [ticket-new-prop [:name]           "Название" "name" nil true]
     [ticket-new-prop [:coordinates :x] "Координата x" "coordinates-x" "(x > - 686)" true]
     [ticket-new-prop [:coordinates :y] "Координата y" "coordinates-y" "(целое число)" true]
     [ticket-new-prop [:price]          "Цена" "price" "(> 0)" true]
     [ticket-new-prop [:discount]       "Скидка" "discount" "(от 0 до 100)" true]
     [ticket-new-prop [:refundable]     "Возвратный" "refundable" nil true
      [{:value true :desc "Да"}
       {:value false :desc "Нет"}] nil]
     [ticket-new-prop [:type]           "Тип" "type" "" false
      ticket-types]
     [ticket-new-prop [:eventId]        "Мероприятие" "eventId" "" true
      nice-events]]))

(defn new-ticket-bot []
  (let [form-valid? @(subscribe [::tsubs/form-valid?])]
    [:div {:class (c :flex [:gap 4])}
     (when form-valid?
       [:button.submitBtn {:class (c [:w-min 100] [:bg :green-500])
                           :on-click #(dispatch [::t/save-ticket-from-form])} "Создать"])
     [:button.cancelBtn {:class (c [:w-min 100])
                         :on-click #(dispatch [::ce/toggle-new])} "Отменить"]]))

(defn new []
  (components/modal
   "Новый билет"
   [new-ticket-top]
   [new-ticket-bot]
   :modal-medium))

(defn edit-ticket-view-top [id]
  (let [events @(subscribe [::esubs/events])
        name-sub @(subscribe [::tsubs/form-path-invalid-message [:name]])
        x-sub @(subscribe [::tsubs/form-path-invalid-message [:coordinates :x]])
        y-sub @(subscribe [::tsubs/form-path-invalid-message [:coordinates :y]])
        creationdate-sub @(subscribe [::tsubs/form-path-invalid-message [:creationDate]])
        price-sub @(subscribe [::tsubs/form-path-invalid-message [:price]])
        discount-sub @(subscribe [::tsubs/form-path-invalid-message [:discount]])
        refundable-sub @(subscribe [::tsubs/form-path-invalid-message [:refundable]])
        type-sub @(subscribe [::tsubs/form-path-invalid-message [:type]])
        eventid-sub @(subscribe [::tsubs/form-path-invalid-message [:eventId]])
        ticket-types @(subscribe [::tsubs/ticket-types])]
    [:div
     [:div
      {:class (c :grid [:grid-cols 2])}
      [:div [components/input-with-init-value id [:name] "Название" "name" nil true]
       [:div {:class (c :text-l)} (str name-sub)]]

      [:div [components/input-with-init-value id [:coordinates :x] "Координата x" "coordinates-x" "(x > - 686)" true]
       [:div {:class (c :text-l)} (str x-sub)]]

      [:div [components/input-with-init-value id [:coordinates :y] "Координата y" "coordinates-y" "целое число" true]
       [:div {:class (c :text-l)} (str y-sub)]]

      [:div [components/input-with-init-value id [:creationDate] "Дата создания" "creation-date" "YYYY-MM-DD" true]
       [:div {:class (c :text-l)} (str creationdate-sub)]]

      [:div [components/input-with-init-value id [:price] "Цена" "price" "(> 0)" true]
       [:div {:class (c :text-l)} (str price-sub)]]

      [:div [components/input-with-init-value id [:discount] "Скидка" "discount" "(от 0 до 100)" true]
       [:div {:class (c :text-l)} (str discount-sub)]]

      [:div [components/input-with-init-value id [:refundable] "Возвратный" "refundable" "true/false" true [{:value true :desc "Да"} {:value false :desc "Нет"}]]
       [:div {:class (c :text-l)} (str refundable-sub)]]

      [:div [components/input-with-init-value id [:type] "Тип" "type" "VIP, USUAL, BUDGETARY, CHEAP" false
             ticket-types]
       [:div {:class (c :text-l)} (str type-sub)]]

      [:div [components/input-with-init-value id [:eventId] "Мероприятие" "event" nil true
             (nice-events events)]
       [:div {:class (c :text-l)} (str eventid-sub)]]]]))

(defn edit-ticket-view-bot []
  (let [form-valid? @(subscribe [::tsubs/form-valid?])]
    [:<>
     (when form-valid?
       [:button.submitBtn
        {:class (c [:w-min 100]) :on-click #(dispatch [::t/update-ticket-from-form])}
        "Изменить"])
     [:button.cancelBtn {:class (c [:w-min 100])
                         :on-click #(dispatch [::t/end-ticket-update])}
      "Отменить"]]))

(defn edit []
  (let [to-edit-id @(subscribe [::tsubs/ticket-update-id])]
    [components/modal
     (str "Изменение билета " to-edit-id)
     [edit-ticket-view-top to-edit-id]
     [edit-ticket-view-bot]
     :modal-medium]))

(defn delete []
  (let [to-delete-id @(subscribe [::tsubs/ticket-to-delete-id])]
    [components/modal
     "Удаление"
     (gstring/format "Вы уверены в удалении билета %s?" to-delete-id)
     [:<>
      [:button.deleteBtn
       {:on-click #(dispatch [::t/delete-ticket to-delete-id])}
       "Удалить"]
      [:button.cancelBtn
       {:on-click #(dispatch [::ce/toggle-delete-false])}
       "Отменить"]]]))
