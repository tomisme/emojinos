(ns dev.emojis
  (:require
   [devcards.core]
   [emojinos.game :as g]
   [emojinos.ai :as ai]
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

     #_"reductions"
     #_(reductions g/apply-effect board (g/build-effects board rules))

     "resolved board"
     (g/resolve-board board rules)}))

(defcard-rg state1-board-post-rules-render
  (e/board-el {:board (g/resolve-board (:board s1) (:rules s1))}))

(defcard-rg frame-render
  [ui-component])
