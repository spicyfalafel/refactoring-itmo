(ns client.subs.tickets
  (:require [re-frame.core :as re-frame :refer [reg-sub]]))

(reg-sub
 ::tickets
 (fn [db _]
   (get-in db [:tickets])))

(reg-sub
 ::tickets-1
 (fn [db _]
   (vals (get-in db [:tickets]))))

(reg-sub
 ::ticket-by-id
 (fn [db [_ id]]
   (get-in db [:tickets id])))

(reg-sub
 ::form-prop
 (fn [db [_ prop-path]]
   (get-in db (into [:form] prop-path))))

(reg-sub
 ::form-valid?
 (fn [db [_]]
   (let [form (get db :form-valid)]
     (or (nil? form)
         (= :ok form)
         (and (or (vector? form) (seq? form)) (empty? form))))))

(reg-sub
 ::form-path-invalid-message
 (fn [db [_ prop-path]]
   (when (not= :ok  (get db :form-valid))
     (first (vec (mapv :message (filter
                                 (fn [error-mp]
                                   (= prop-path (into [] (:path error-mp))))
                                 (get db :form-valid))))))))

(reg-sub
 ::ticket-update-id
 (fn [db [_]]
   (get-in db [:ticket :update-id])))

(reg-sub
 ::ticket-to-delete-id
 (fn [db [_]]
   (get-in db [:ticket :to-delete])))

(reg-sub
 ::count-tickets
 (fn [db _]
   (get db :count-tickets)))

(reg-sub
 ::ticket-types
 (fn [db _]
   (get db :ticket-types)))

(reg-sub
 ::tickets-discount-sum
 (fn [db [_]]
   (get db :tickets-discount-sum)))

(reg-sub
 ::tickets-discount-count
 (fn [db [_]]
   (get db :tickets-discount-count)))

(reg-sub
 ::tickets-types-count
 (fn [db [_]]
   (get db :tickets-types-count)))

(reg-sub
 ::ticket-discount-count-opened
 (fn [db [_]]
   (get db :ticket-discount-count-opened)))

