(ns emojinos.game)

;; position = [x y]
;; tile = [emoji x y]
;; board = #{} of tiles
;; rule = [left right]
;; path = {} of :direction, :tiles, :walking?

(defn pos-neighbors [x y]
  #{[(inc x) y]
    [x (inc y)]
    [(dec x) y]
    [x (dec y)]})

(defn filled-positions [board]
  (into #{}
        (map (fn [[_ x y]]
               [x y])
             board)))

(defn targets [board]
  (let [filled (filled-positions board)]
    (reduce
     (fn [coll [_ x y]]
       (loop [to-check (seq (pos-neighbors x y))
              empty-neighbors #{}]
           (if (empty? to-check)
             (into coll empty-neighbors)
             (let [checking (first to-check)]
               (recur (rest to-check)
                      (if (contains? filled checking)
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

(defn get-neighbors [start-x start-y board]
  (into #{}
        (filter boolean
                (map (fn [[x y]]
                       (get-tile-at x y board))
                     (pos-neighbors start-x start-y)))))

(defn root? [rule tile]
  (let [[[root]] rule
        [emoji] tile]
    (= root emoji)))

(defn next-step-in-paths [board rule-left paths step] ;; => paths
  (into #{}
        (map (fn [{:keys [direction tiles walking?] :as path}]
               (if (not walking?)
                 (assoc path :walking? false)
                 (let [[_ x y] (last tiles)
                       [dx dy] direction
                       next-tile (get-tile-at (+ x dx) (+ y dy) board)]
                   (if (and next-tile
                            (= (get next-tile 0)
                               (get rule-left step)))
                     (update path :tiles conj next-tile)
                     (assoc path :walking? false)))))
             paths)))

(defn get-effect [board rule tile] ;; => nil || {}
  (when (root? rule tile)
    (let [[root-emoji root-x root-y] tile
          [rule-left rule-right] rule
          total-steps (count rule-left)]
      (loop [step 0
             paths (into #{}
                         (map (fn [direction]
                                {:direction direction
                                 :tiles [tile]
                                 :walking? true})
                              [[0 1]
                               [1 0]
                               [0 -1]
                               [-1 0]]))]
        (if (= step total-steps)
          {:rule rule
           :total-steps total-steps
           :paths paths}
          (recur (inc step)
                 (next-step-in-paths board rule-left paths (inc step))))))))

(defn get-rule-fx [board rule] ;; => [] of effects
  (reduce (fn [fx tile]
            (if-let [effect (get-effect board rule tile)]
              (conj fx effect)
              fx))
          []
          board))

(defn get-board-fx [board rules] ;; => effects
  (reduce (fn [fx rule]
            (concat fx (get-rule-fx board rule)))
          []
          rules))

(defn apply-effects [board effects] ;; => board
  board)

(defn resolve-board [board rules] ;; => board
  (apply-effects board (get-board-fx board rules)))













(defn remove-from-vec
  "Returns a new vector with the element at 'index' removed.

  (remove-from-vec [:a :b :c] 1)  =>  [:a :c]"
  [v index]
  (vec (concat (subvec v 0 index) (subvec v (inc index)))))

(defn place-tile
  [state {:keys [player idx x y]}]
  (let [emoji (get-in state [:p1 :hand idx])]
    (-> state
        (update-in [:p1 :hand] remove-from-vec idx)
        (update :board conj [emoji x y (= :p1 player)]))))
