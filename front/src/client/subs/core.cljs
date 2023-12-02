(ns client.subs.core
  (:require
   [re-frame.core :as re-frame :refer [reg-sub]]))

(reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))

(reg-sub
 ::mode
 (fn [db [_]]
   (get db :mode)))

(reg-sub
 ::toggle-new
 (fn [db [_]]
   (get db :toggle-new)))

(reg-sub
 ::toggle-delete
 (fn [db [_]]
   (get db :toggle-delete)))

(reg-sub
 ::current-page
 (fn [db [_]]
   (get-in db [:paging :current-page])))

(reg-sub
 ::last-page
 (fn [db [_]]
   (let [mode (:mode db)
         entity  (if (= mode :tickets) :count-tickets :count-events)]
     (js/Math.ceil
      (double
       (/ (get-in db [entity])
          (get-in db [:paging :page-size])))))))

(reg-sub
 ::toggle-change
 (fn [db [_]]
   (get-in db [:toggle-change])))

(reg-sub
 ::page-size
 (fn [db [_]]
   (get-in db [:paging :page-size])))

(reg-sub
 ::filters
 (fn [db [_ prop]]
   (get-in db [:filters (:mode db) prop])))

(reg-sub
 ::initialized?
 (fn [db _]
   (and (find db :events)
        (find db :tickets))))

(reg-sub
 ::reloading
 (fn [db _]
   (get db :loading)))


(reg-sub
 ::sortings
 (fn [db [_ mode]]
   (get-in db [(if (= mode :tickets) :ticket :event) :sorting])))
