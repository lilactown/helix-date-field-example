(ns town.lilac.date-field.app
  (:require
   ["react-dom/client" :as rdom]
   [clojure.string :as string]
   [helix.core :refer [$ defnc]]
   [helix.dom :as d]
   [helix.hooks :as hooks]))


(defn valid-date-format?
  [date]
  (or (empty? date)
      ;; FYI this is a bad way to validate dates.
      ;; you should rely on your underlying date-time library
      ;; for this validation but for the blogpost this is
      ;; good enough
      (boolean (re-matches #"[\d]{2}\-[\d]{2}\-[\d]{4}" date))))


(defn dates-are-ascending?
  [a b]
  ;; naive date comparator
  ;; converts DD-MM-YYYY to yyyy-mm-dd and compares the strings
  (let [[d-a m-a y-a]  (string/split a #"-")
        [d-b m-b y-b]  (string/split b #"-")
        comp- (compare (string/join "-" [y-a m-a d-a]) (string/join "-" [y-b m-b d-b]))]
    (or (neg? comp-)
        (zero? comp-))))


(defnc date-field
  [{:keys [error on-change valid? value]}]
  (d/div
   (d/input
    {:class ["input" (when (or (not valid?) (some? error))
                       "is-danger")]
     :type "text"
     :value value
     :placeholder "DD-MM-YYYY"
     :on-change #(let [v (.. % -target -value)
                       valid? (valid-date-format? v)]
                   (on-change v valid?))})
   (when (not valid?)
     (d/p
      {:class "help is-danger"}
      "Incorrect date format, use DD-MM-YYYY"))
   (when error
     (d/p
      {:class "help is-danger"}
      error))))


(defnc date-range
  [{:keys [on-change start start-valid? end end-valid? range-valid?]}]
  (d/div
   {:class "is-flex is-align-items-center"}
   (d/p
    {:class "pr-3"}
    "From")
   (d/span
    {:class "pr-3"}
    ($ date-field
       {:on-change (fn [v date-valid?]
                     (let [range-valid? (dates-are-ascending? v end)]
                       (on-change {:field :start
                                   :value v
                                   :valid? date-valid?}
                                  range-valid?)))
        :valid? start-valid?
        :value start
        :error (when (not range-valid?)
                 "Incorrect date range. Start date can't be before end date.")}))
   (d/p
    {:class "px-3"}
    "To")
   (d/span
    {:class "px-3"}
    ($ date-field
       {:on-change (fn [v date-valid?]
                     (let [range-valid? (dates-are-ascending? start v)]
                       (on-change {:field :end
                                   :value v
                                   :valid? date-valid?}
                                  range-valid?)))
        :valid? end-valid?
        :value end
        :error (when (not range-valid?)
                 "Incorrect date range. Start date can't be before end date.")}))))


(defnc submit-button
  [{:keys [disabled? on-click]}]
  (d/button
   {:class "button my-3 is-dark"
    :data-testid "submit-btn"
    :disabled disabled?
    :on-click on-click}
   "submit"))


(defnc main-panel
  []
  (let [[{:keys [date-value date-valid?]} set-date] (hooks/use-state
                                                     {:date-value ""
                                                      :date-valid? true})
        [{:keys [start
                 end
                 range-valid?]} set-range] (hooks/use-state
                                            {:start {:value ""
                                                     :valid? true}
                                             :end {:value ""
                                                   :valid? true}
                                             :range-valid? true})]
    (d/div
     {:class "container my-6"}
     ($ date-field
        {:value date-value
         :valid? date-valid?
         :on-change (fn [v valid?]
                      (set-date {:date-value v
                                 :date-valid? valid?}))})
     ($ date-range
        {:start (:value start)
         :start-valid? (:valid? start)
         :end (:value end)
         :end-valid? (:valid? end)
         :range-valid? range-valid?
         :on-change (fn [{:keys [field value valid?]} range-valid?]
                      (set-range assoc
                                 field {:value value
                                        :valid? valid?}
                                 :range-valid? range-valid?))})
     ($ submit-button
        {:disabled? (not (and date-valid? range-valid?))
         :on-click (fn [_]
                     (set-date {:date-value ""
                                :date-valid? true})
                     (set-range {:start {:value "" :valid? true}
                                 :end {:value "" :valid? true}
                                 :range-valid? true}))}))))
