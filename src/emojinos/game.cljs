(ns emojinos.game)

;; position = [x y]
;; tile = [emoji x y]
;; board = #{} of tiles
;; rule = [left right]
;; path = {} of :direction, :tiles, :walking?
;; effect = {} of tiles => emojis

(def all-directions
  [[0 1]
   [1 0]
   [0 -1]
   [-1 0]])

(defn pos-direction? [direction]
  (or (= [0 1] direction)
      (= [1 0] direction)))

(defn get-neighbors [x y]
  [[(inc x) y]
   [x (inc y)]
   [(dec x) y]
   [x (dec y)]])

(defn get-filled-positions [board] ;; => #{} of positions
  (into #{}
        (map (fn [[_ x y]]
               [x y])
             board)))

(defn get-targets [board] ;; => #{} of positions
  (let [filled-positions (get-filled-positions board)]
    (reduce (fn [targets [_ x y]]
              (loop [tiles-to-check (get-neighbors x y)
                     empty-neighbors #{}]
                (if (empty? tiles-to-check)
                  (into targets empty-neighbors)
                  (let [checking (first tiles-to-check)]
                    (recur (rest tiles-to-check)
                           (if (contains? filled-positions checking)
                             empty-neighbors
                             (conj empty-neighbors checking)))))))
            #{}
            board)))

(defn get-tile-at [x y board] ;; => nil || tile
  (some (fn [tile]
          (let [[_ x2 y2] tile]
            (when (and (= x x2)
                       (= y y2))
              tile)))
        board))

(defn root-tile? [rule tile]
  (let [[[root]] rule
        [emoji] tile]
    (= root emoji)))

(defn paths->effect [paths rule-right] ;; => nil || effect
  (let [rule-size (count rule-right)]
    (reduce (fn [effect {:keys [tiles direction]}]
              (if (< (count tiles) rule-size)
                effect
                (let [path-effect (zipmap tiles
                                          (if (pos-direction? direction)
                                            rule-right
                                            (reverse rule-right)))]
                  (if effect
                    (conj effect path-effect)
                    path-effect))))
            nil
            paths)))

(defn take-step-in-paths [board rule-left paths step] ;; => paths
  (map (fn [{:keys [direction tiles walking?] :as path}]
         (if (not walking?)
           path
           (let [[_ x y] (last tiles)
                 [dx dy] direction
                 next-tile (get-tile-at (+ x dx) (+ y dy) board)]
             (if (and next-tile
                      (= (get next-tile 0)
                         (get rule-left step)))
               (update path :tiles conj next-tile)
               (assoc path :walking? false)))))
       paths))

(defn get-tile-effect [board rule tile] ;; => nil || effect
  (when (root-tile? rule tile)
    (let [[root-emoji root-x root-y] tile
          [rule-left rule-right] rule
          total-steps (count rule-left)]
      (loop [step 0
             paths (map (fn [direction]
                          {:direction direction
                           :tiles [tile]
                           :walking? true})
                        all-directions)]
        (if (= step total-steps)
          (paths->effect paths rule-right)
          (recur (inc step)
                 (take-step-in-paths board rule-left paths (inc step))))))))

(defn get-rule-effect [board rule] ;; => nil || effect
  (reduce (fn [rule-effect tile]
            (if-let [tile-effect (get-tile-effect board rule tile)]
              (if rule-effect
                (conj rule-effect tile-effect)
                tile-effect)
              rule-effect))
          nil
          board))

(defn apply-effect [starting-board effect] ;; => board
  (reduce (fn [board [tile emoji]]
            (if emoji
              (-> board
                  (disj tile)
                  (conj [emoji (nth tile 1) (nth tile 2)]))
              (disj board tile)))
          starting-board
          effect))

(defn build-effects [starting-board rules]
  (loop [board starting-board
         effects []]
    (let [effect (some #(get-rule-effect board %)
                       rules)]
      (if (empty? effect)
        effects
        (recur (apply-effect board effect)
               (conj effects effect))))))

(defn resolve-board [board rules]
  (reduce apply-effect board (build-effects board rules)))
