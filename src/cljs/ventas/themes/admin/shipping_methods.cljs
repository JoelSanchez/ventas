(ns ventas.themes.admin.shipping-methods
  (:require
   [re-frame.core :as rf]
   [ventas.components.base :as base]
   [ventas.components.table :as table]
   [ventas.server.api.admin :as api.admin]
   [ventas.i18n :refer [i18n]]
   [ventas.themes.admin.shipping-methods.edit]
   [ventas.themes.admin.skeleton :as admin.skeleton]
   [ventas.routes :as routes]))

(def state-key ::state)

(rf/reg-event-fx
 ::remove
 (fn [_ [_ id]]
   {:dispatch [::api.admin/admin.entities.remove
               {:params {:id id}
                :success [::remove.next id]}]}))

(rf/reg-event-db
 ::remove.next
 (fn [db [_ id]]
   (update-in db
              [state-key :table :rows]
              (fn [items]
                (remove #(= (:id %) id)
                        items)))))

(defn- action-column [{:keys [id]}]
  [:div
   [base/button {:icon true
                 :on-click #(rf/dispatch [::remove id])}
    [base/icon {:name "remove"}]]])

(defn- footer []
  [base/button {:on-click #(routes/go-to :admin.shipping-methods.edit :id 0)}
   (i18n ::create)])

(rf/reg-event-fx
 ::fetch
 (fn [{:keys [db]} [_ state-path]]
   (let [{:keys [page items-per-page sort-direction sort-column] :as state} (table/get-state db state-path)]
     {:dispatch [::api.admin/admin.entities.list
                 {:success ::fetch.next
                  :params {:type :shipping-method
                           :pagination {:page page
                                        :items-per-page items-per-page}
                           :sorting {:direction sort-direction
                                     :field sort-column}}}]})))

(rf/reg-event-db
 ::fetch.next
 (fn [db [_ {:keys [items total]}]]
   (-> db
       (assoc-in [state-key :table :rows] items)
       (assoc-in [state-key :table :total] total))))

(defn- content []
  [:div.admin-shipping-methods__table
   [table/table [state-key :table]]])

(defn- page []
  [admin.skeleton/skeleton
   [:div.admin__default-content.admin-shipping-methods__page
    [content action-column]]])

(rf/reg-event-fx
 ::init
 (fn [_ _]
   {:dispatch [::table/init [state-key :table]
               {:fetch-fx [::fetch]
                :columns [{:id :name
                           :label (i18n ::name)
                           :component (partial table/link-column :admin.shipping-methods.edit :id :name)}
                          {:id :actions
                           :label (i18n ::actions)
                           :component action-column
                           :width 110}]
                :footer footer}]}))

(routes/define-route!
  :admin.shipping-methods
  {:name ::page
   :url "shipping-methods"
   :component page
   :init-fx [::init]})
