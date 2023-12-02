(ns client.views.tickets
  (:require
   [re-frame.core :as re-frame :refer [dispatch subscribe]]
   [client.events.ticket-events :as t]
   [client.events.common-events :as ce]
   [client.debounce] ; to reg event :)
   [client.views.myclasses :as cls]
   [goog.string :as gstring]
   [goog.string.format]
   [client.views.mycomponents :as components]

   [client.subs.tickets :as tsubs]
   [client.subs.events :as esubs]
   [client.subs.core :as subs])
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

(defn type-view [type]
  (cond
    (= type "VIP")
    [:i.fa-regular.fa-star]

    (= type "USUAL")
    [:i.fa-solid.fa-check]

    (= type "BUDGETARY")
    [:i.fa-solid.fa-wheelchair-move]

    (= type "CHEAP")
    [:i.fa-regular.fa-star]))

(defn ticket-new-prop [prop-path label label-id descr required?
                       & [select-values default-value]]
  (let [current-value  @(re-frame.core/subscribe [::tsubs/form-prop prop-path])
        invalid-message @(re-frame.core/subscribe [::tsubs/form-path-invalid-message prop-path])
        on-change-fn
        #(dispatch [:dispatch-debounce
                    {:delay 300
                     :event [::t/change-ticket-and-validate prop-path (.. % -target -value)]}])]
    [:div {:class (c [:px 5] :flex :flex-col)}

     [:label {:for label-id} label (when required? [components/required-*])]
     (if select-values
       (components/selector
        select-values
        on-change-fn
        {:cls (c :w-full [:mb 2])
         :default-value default-value})

       [:input {:name label-id
                :id label-id
                :class (c :border [:h 10] :text-2xl)
                :maxLength 30
                :on-change
                on-change-fn
                :placeholder (str current-value)}])

     [:div
      [:label {:for label-id
               :class (c :text-lg :font-light :italic)} (when descr descr)]]
     [:div {:class (c :text-sm)}
      (when invalid-message
        invalid-message)]]))

(defn edit-ticket-icon []
  [:span [:i.fa-solid.fa-pen-to-square
          {:class (c [:px 3])}]])

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

(defn one-ticket [id]
  (let [ticket @(subscribe [::tsubs/ticket-by-id id])
        event  @(subscribe [::esubs/event-by-id (:eventId ticket)])]
    ^{:key id}
    [:div
     [:div {:class [(c [:bg "#FAFAFA"]
                       [:border :current]
                       :flex-row
                       :flex
                       :justify-between
                       [:p 2]
                       [:m 2]
                       [:hover :shadow-inner [:bg :gray-200]]
                       [:rounded :xl])]}
      [:div {:class (c [:w-max 100])}
       [:div
        [:div "ID: " id]
        [:span #_{:class (c :text-sm)}
         (:creationDate ticket) " " (type-view (:type ticket)) " " [:span (:type ticket)] " "]
        [:div  {:class (c [:w-max 100])}
         [:span {:class (c :text-xl :text-bold)}
          [:span.truncate {:class (c [:h 9] [:h-max 9]) :title (:name event)}
           (if (:name event)
             (if (> (count (:name event)) 15)
               (str (apply str (take 15 (:name event))) "...") (:name event))
             "Неизвестное мероприятие")]]]]
       [:span {:class (c :text-sm [:h 9] [:h-max 9])
               :title (:name ticket)}
        (str "Название билета: "
             (if (> (count (:name ticket)) 15)
               (str (apply str (take 15 (:name ticket))) "...") (:name ticket)))]
       [:div {:class (c :text-sm)} (if (:refundable ticket) "Возвратный" "Невозвратный")]]
      [:div  {:class (c [:w-max 100])}
       [:div
        "СКИДКА: " (:discount ticket) "%"]
       [:div "ЦЕНА: " (:price ticket)]
       [:div (str "(" (:x (:coordinates ticket)) ","  (:y (:coordinates ticket)) ")")]]
      [:div {:class (c :flex :flex-col :justify-center [:gap 5])}
       [:div
        {:class [cls/base-class (c :cursor-pointer)]
         :on-click #(dispatch [::t/start-ticket-update id])}
        (edit-ticket-icon)
        "Изменить"]
       [:div
        {:class [cls/base-class (c :cursor-pointer)]
         :on-click #(dispatch [::ce/toggle-delete id])}
        [components/delete-icon]
        "Удалить"]]]]))

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

(defn tickets-view []
  (let [tickets @(re-frame/subscribe [::tsubs/tickets-1])
        modal-opened? @(subscribe [::subs/toggle-new])
        modal-delete-opened? @(subscribe [::subs/toggle-delete])
        to-delete-id @(subscribe [::tsubs/ticket-to-delete-id])
        modal-edit-opened?   @(subscribe [::subs/toggle-change])
        to-edit-id @(subscribe [::tsubs/ticket-update-id])]

    [:div {:class (c :w-full)}
     [components/paging]
     (when modal-opened?
       (components/modal
        "Новый билет"
        [new-ticket-top]
        [new-ticket-bot]
        :modal-medium))

     [:div {:class (c :grid [:grid-cols 2])}
      [:div {:class [cls/div-center]
             :on-click #(dispatch [::ce/toggle-new])}
       "НОВЫЙ"]
      (doall
       (for [ticket tickets]
         ^{:key (:id ticket)}
         [one-ticket (:id ticket)
          (fn [] (dispatch [::t/delete-ticket (:id ticket)]))]))
      (when modal-edit-opened?
        [components/modal
         (str "Изменение билета " to-edit-id)
         [edit-ticket-view-top to-edit-id]
         [edit-ticket-view-bot]
         :modal-medium])
      (when modal-delete-opened?
        [components/modal
         "Удаление"
         (gstring/format "Вы уверены в удалении билета %s?" to-delete-id)
         [:<>
          [:button.deleteBtn
           {:on-click #(dispatch [::t/delete-ticket to-delete-id])}
           "Удалить"]
          [:button.cancelBtn
           {:on-click #(dispatch [::ce/toggle-delete-false])}
           "Отменить"]]])]]))
