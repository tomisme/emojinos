(ns dev.emojis
  (:require
   [reagent.core :as r]
   [emojinos.game :as g]
   [emojinos.ui.elements :as e]
   [emojinos.ui.frame :refer [ui-component]]
   [reanimated.core :as anim])
  (:require-macros
   #_[clojure.test :refer [is testing]]
   [devcards.core :refer [defcard defcard-rg #_deftest]]))

(defcard-rg tiles
  (into [:div {:style {:display "flex"
                       :flex-wrap "wrap"}}]
        (for [x [
                 "💓" "💞" "🍇" "🌱" "🌧️" "🌻" "🍄" "🔥" "🌷" "🍚"
                 "🥰" "🐦" "🌏" "🐻" "🍔" "⚽" "⏳" "🧱" "💣" "📺"
                 "🌼" "🌾" "🌈" "🏥" "⛩" "🗿" "🤔" "🐥" "🕷"
                 "💓" "💞" "🍇" "🌱" "🌧️" "🌻" "🍄" "🔥" "🌷" "🍚"
                 "🌼" "🌾" "🌈" "🏥" "⛩" "🗿" "🤔" "🐥" "🕷"]]

          [:div {:style {:margin 5}}
           (e/tile-el {:emoji x
                       :white? (rand-nth [true false])})])))

(def s1
  {:board #{["🌧️" 0 0]
            ["🌱" 0 1]
            ["🌷" 0 -1]
            ["🌱" -1 0]
            ["🌱" 1 0]
            ["🔥" -2 0]
            ["🔥" -4 0]}
   :rules [
           [["🌈" "🌱"] [nil "🍇"]]
           [["🔥" "🌱" "🌧️"] [nil "🌻" "🌈"]]]})

(defcard state1 s1)

(defcard-rg state1-pre-resolve-board-render
  [:div
   (e/board-el {:board (:board s1)})])

(defcard-rg state1-rules-render
  (e/rules-el (:rules s1)))

(defcard state1-resolution-details
  (let [board (:board s1)
        rules (:rules s1)]
    {
     "board" board
     "rules" rules

     "effects"
     (g/build-effects board rules)

     "reductions"
     (reductions g/apply-effect board (g/build-effects board rules))

     "resolved board"
     (g/resolve-board board rules)

     "double resolution reductions"
     (reductions g/apply-effect
                 (g/resolve-board board rules)
                 (g/build-effects (g/resolve-board board rules) rules))}))

(defcard-rg state1-board-post-rules-render
  (e/board-el {:board (g/resolve-board (:board s1) (:rules s1))}))

(defn broken-swapper [data]
  (if (= :a (:letter @data))
    [:div
     [anim/timeline
      [:span "is A"]
      1000
      #(swap! data assoc :letter :b)]]
    [:div
     [anim/timeline
      [:span "is B"]
      1000
      #(swap! data assoc :letter :a)]]))

(defcard-rg broken-swap
  (fn [data]
    [broken-swapper data])
  (r/atom {:letter :a})
  {:inspect-data true})

(defcard-rg animate
  (let [board #{["🔥" 0 0]
                ["🌱" 1 0]
                ["🌱" 2 0]}
        rules [[["🔥" "🌱"] ["🔥" "🔥"]]]
        effects (g/build-effects board rules)]
    (into [anim/timeline]
          (interpose 1000
                     (for [board (reductions g/apply-effect board effects)]
                       (e/board-el {:board board
                                    :place-tile! #()}))))))

(defcard-rg variables-render
  [:div {:style {:display "flex"}}
   (e/tile-el {:color :red})
   [:span {:style {:margin 5}}]
   (e/tile-el {:color :green})
   [:span {:style {:margin 5}}]
   (e/tile-el {:color :blue})])

(defcard-rg frame-render
  [ui-component])
