(ns town.lilac.date-field.app-test
  (:require
   ["@testing-library/react" :as rtl]
   [cljs.test :refer [deftest is use-fixtures]]
   [helix.core :refer [$ defnc]]
   [helix.hooks :as hooks]
   [town.lilac.date-field.app :as app]))


(use-fixtures :each
  {:after rtl/cleanup})


(defnc date-field-container
  [{:keys [on-change]}]
  (let [[{:keys [valid? value]} set-state] (hooks/use-state
                                            {:value ""
                                             :valid? true})]
    ($ app/date-field
       {:on-change (fn [value valid?]
                     (set-state {:value value :valid? valid?})
                     (on-change value valid?))
        :valid? valid?
        :value value})))


(deftest date-field
  (let [*last-change (atom {})
        result (rtl/render ($ date-field-container
                              {:on-change (fn [value valid?]
                                            (reset! *last-change
                                                    {:value value
                                                     :valid? valid?}))}))
        input-el (.queryByRole result "textbox")]
    (is input-el "Smoke test: found textbox")

    (rtl/fireEvent.change input-el #js {:target #js {:value "12-12-123"}})
    (is (= {:value "12-12-123" :valid? false} @*last-change)
        "Invalid date triggers event with `valid?` false")
    (is (.queryByText result "Incorrect date format, use DD-MM-YYYY")
        "Error message is present")

    (rtl/fireEvent.change input-el #js {:target #js {:value "12-12-1234"}})
    (is (= {:value "12-12-1234" :valid? true} @*last-change)
        "Valid date triggers event with `valid?` true")
    (is (not (.queryByText result "Incorrect date format, use DD-MM-YYYY"))
        "Error message is hidden when valid")

    (rtl/fireEvent.change input-el #js {:target #js {:value ""}})
    (is (= {:value "" :valid? true} @*last-change)
        "Blank value is valid")))


(defnc date-range-container
  [{:keys [on-change]}]
  (let [[{:keys [start
                 end
                 range-valid?]} set-state] (hooks/use-state
                                            {:start {:value ""
                                                     :valid? true}
                                             :end {:value ""
                                                   :valid? true}
                                           :range-valid? true})]
    ($ app/date-range
       {:start (:value start)
        :start-valid? (:valid? start)
        :end (:value end)
        :end-valid? (:valid? end)
        :range-valid? range-valid?
        :on-change (fn [{:keys [field value valid?] :as m}
                        range-valid?]
                     (on-change m range-valid?)
                     (set-state assoc
                                field {:value value
                                       :valid? valid?}
                                :range-valid? range-valid?))})))


(deftest date-range
  (let [*last-change (atom [])
        result (rtl/render ($ date-range-container
                              {:on-change (fn [m range-valid?]
                                            (reset! *last-change
                                                    [m range-valid?]))}))
        [start-el end-el] (.queryAllByRole result "textbox")]
    (is start-el "Smoke test: found start textbox")
    (is end-el "Smoke test: found end textbox")

    (rtl/fireEvent.change start-el #js {:target #js {:value "12-12-123"}})
    (is (= [{:field :start :value "12-12-123" :valid? false}
            false]
           @*last-change))
    (is (.queryByText result "Incorrect date format, use DD-MM-YYYY")
        "Error message is present")

    (rtl/fireEvent.change end-el #js {:target #js {:value "12-12-123"}})
    (is (= [{:field :end :value "12-12-123" :valid? false}
            true]
           @*last-change))
    (is (= 2 (count (.queryAllByText result "Incorrect date format, use DD-MM-YYYY")))
        "Error message is present for each invalid date field")

    (rtl/fireEvent.change start-el #js {:target #js {:value "12-12-1234"}})
    (is (= [{:field :start :value "12-12-1234" :valid? true}
            false]
           @*last-change))
    (is (.queryByText result "Incorrect date format, use DD-MM-YYYY")
        "One error message is still present after correcting start field")

    (rtl/fireEvent.change end-el #js {:target #js {:value "12-12-1234"}})
    (is (= [{:field :end :value "12-12-1234" :valid? true}
            true]
           @*last-change))
    (is (not (.queryByText result "Incorrect date format, use DD-MM-YYYY"))
        "No error message is present after correcting start and end field")

    (rtl/fireEvent.change end-el #js {:target #js {:value "12-12-1233"}})
    (is (= [{:field :end :value "12-12-1233" :valid? true}
            false]
           @*last-change))
    (is (not (.queryByText result "Incorrect date format, use DD-MM-YYYY"))
        "No date format error message when entering an invalid range.")
    (is (seq
         (.queryAllByText
          result
          "Incorrect date range. Start date can't be before end date."))
        "Incorrect date range message shown.")))


(deftest main-panel
  (let [result (rtl/render ($ app/main-panel))
        [field-el start-el end-el] (.getAllByRole result "textbox")
        submit-el (.queryByTestId result "submit-btn")]
    (is submit-el "Smoke test: found submit button")
    (rtl/fireEvent.change field-el #js {:target #js {:value "12-12-123"}})
    (is (.-disabled submit-el) "Submit button is disabled when field invalid")

    (rtl/fireEvent.change field-el #js {:target #js {:value "12-12-1234"}})
    (is (not (.-disabled submit-el))
        "Submit button is not disabled when field valid")

    (rtl/fireEvent.change start-el #js {:target #js {:value "12-12-1234"}})
    (rtl/fireEvent.change end-el #js {:target #js {:value "12-12-1233"}})
    (is (.-disabled submit-el)
        "Submit button is disabled when range is invalid")

    (rtl/fireEvent.change end-el #js {:target #js {:value "12-12-1235"}})
    (is (not (.-disabled submit-el))
        "Submit button is not disabled when field valid")

    (rtl/fireEvent.click submit-el)
    (is (= "" (.-value field-el)) "Field is cleared after submit")
    (is (= "" (.-value start-el)) "Start is cleared after submit")
    (is (= "" (.-value end-el)) "End is cleared after submit")))
