(ns client.views.event.modal
  (:require
   [re-frame.core :as re-frame :refer [dispatch subscribe]]
   [client.events.events-events :as e]
   [client.events.common-events :as ce]
   [client.debounce]
   [goog.string :as gstring]
   [goog.string.format]
   [client.views.mycomponents :as components]
   [client.subs.events :as esubs])
  (:require-macros [stylo.core :refer [c]]))

(defn event-new-prop [prop-path label label-id descr required?
                      & [select-values]]
  ((components/entity-new-prop
    ::esubs/event-form-prop
    ::esubs/event-form-path-invalid-message
    ::e/change-event-and-validate)
   prop-path label label-id descr required? select-values))

(defn new-event-top []
  (let [event-types @(subscribe [::esubs/event-types])]
    [:div
     [event-new-prop [:name] "Название" "name" nil true]
     [event-new-prop [:date] "Дата мероприятия" "date" nil false]
     [event-new-prop [:minAge] "Минимальный возраст" "minAge" nil true]
     [event-new-prop [:eventType] "Тип мероприятия" "type" nil false
      event-types]]))

(defn new-event-bot []
  (let [form-valid? @(subscribe [::esubs/events-form-valid?])]
    [:div {:class (c :flex [:gap 4])}
     (when form-valid?
       [:button.submitBtn {:class (c [:w-min 100] [:bg :green-500])
                           :on-click #(dispatch [::e/save-event-from-form])} "Создать"])
     [:button.cancelBtn {:class (c [:w-min 100])
                         :on-click #(dispatch [::ce/toggle-new])} "Отменить"]]))

(defn new []
  [components/modal
   "Новое мероприятие"
   [new-event-top]
   [new-event-bot]
   :modal-medium])

(defn edit-event-view-top [id]
  (let [name-sub @(subscribe [::esubs/event-form-path-invalid-message [:name]])
        date-sub @(subscribe [::esubs/event-form-path-invalid-message [:date]])
        min-age-sub @(subscribe [::esubs/event-form-path-invalid-message [:minAge]])
        type-sub @(subscribe [::esubs/event-form-path-invalid-message [:eventType]])
        event-types @(subscribe [::esubs/event-types])]
    [:div
     {:class (c :grid [:grid-cols 2])}
     [components/input-with-init-value id [:name] "Название мероприятия" "name" nil true]
     [:div {:class (c :text-l)} (str name-sub)]

     [components/input-with-init-value id [:date] "Дата мероприятия" "date" "YYYY-MM-DD" false]
     [:div {:class (c :text-l)} (str date-sub)]
     [components/input-with-init-value id [:minAge] "Минимальный возраст" "minAge" "Целое число больше 0" true]
     [:div {:class (c :text-l)} (str min-age-sub)]

     [components/input-with-init-value id [:eventType] "Тип мероприятия" "event-type"
      "CONCERT, BASEBALL, BASKETBALL, THEATRE_PERFORMANCE" false
      event-types]
     [:div {:class (c :text-l)} (str type-sub)]]))

(defn edit-event-view-bot []
  (let [form-valid? @(subscribe [::esubs/events-form-valid?])]
    [:<>
     (when form-valid?
       [:button.submitBtn
        {:class (c [:w-min 100])
         :on-click #(dispatch [::e/update-event-from-form])}
        "Изменить"])
     [:button.cancelBtn {:class (c [:w-min 100])
                         :on-click #(dispatch [::e/end-event-update])}
      "Отменить"]]))

(defn edit []
  (let [event-to-edit-id @(subscribe [::esubs/event-update-id])]
    [components/modal
     (str "Изменение мероприятия " event-to-edit-id)
     [edit-event-view-top event-to-edit-id]
     [edit-event-view-bot]
     :modal-medium]))

(defn delete []
  (let [event-to-delete-id @(subscribe [::esubs/event-to-delete-id])]
    [components/modal
     "Удаление"
     (gstring/format "Вы уверены в удалении мероприятия %s?" event-to-delete-id)
     [:<>
      [:button.deleteBtn
       {:on-click #(dispatch [::e/delete-event event-to-delete-id])}
       "Удалить"]
      [:button.cancelBtn
       {:on-click #(dispatch [::e/toggle-delete-false])}
       "Отменить"]]]))
