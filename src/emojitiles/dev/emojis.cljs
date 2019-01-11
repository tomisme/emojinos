(ns emojitiles.dev.emojis
  (:require
   [devcards.core])
  (:require-macros
   [devcards.core :refer [defcard defcard-rg]]))

(def all-emojis
  ["🙂"
   "🍽"
   "🍽"
   "🥗"
   "🧱"
   "🔪 "])

(defcard all-emojis all-emojis)

(defn card
  [x]
  [:div {:style {:display "flex"
                 :justify-content "center"
                 :border "10px solid"
                 :border-radius 20
                 :width 100
                 :height 100}}
   [:div {:style {:display "flex"
                  :align-items "center"
                  :font-size 60}}
    x]])


(defcard-rg card
  (into [:div {:style {:display "flex"
                       :flex-wrap "wrap"}}]
        (for [x all-emojis]
          [:div {:style {:margin 5}}
           (card x)])))
