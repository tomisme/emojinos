(ns emojinos.game)

(defn pos-neighbors
  [x y]
  #{[(inc x) y]
    [x (inc y)]
    [(dec x) y]
    [x (dec y)]})

(defn filled-positions
  [board]
  (into #{}
        (map (fn [[_ x y]]
               [x y])
             board)))

(defn targets
  [board]
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

(defn remove?
  [tile board rules]
  (let [emoji (first tile)]
    (reduce (fn [accum rule]
              (or accum
                  (case (:type rule)
                    :adj (and (= emoji (:adj rule))
                              (:consume-adj rule)))))
            false
            rules)))

(defn get-tile-at
  [x y board]
  (some (fn [tile]
          (let [[_ x2 y2] tile]
            (when (and (= x x2)
                       (= y y2))
              tile)))
        board))

(defn get-neighbors
  [start-x start-y board]
  (into #{}
        (map (fn [[x y]]
               (get-tile-at x y board))
             (pos-neighbors start-x start-y))))

(defn a-neighbor-matches?
  [tile board rule]
  (let [[_ x y] tile
        {:keys [adj]} rule
        neighbors (get-neighbors x y board)]
    (reduce (fn [accum [emoji]]
              (or accum
                  (= emoji adj)))
            false
            neighbors)))

(defn get-changes
  [tile board rules]
  (let [emoji (first tile)]
    (reduce (fn [accum rule]
              (conj accum
                (case (:type rule)
                  :adj (if (and (= emoji (:base rule))
                                (a-neighbor-matches? tile board rule))
                         (:base-to rule)
                         nil))))
            []
            rules)))

(defn get-transformations
  [board rules]
  (into #{}
        (map (fn [tile]
               (if (remove? tile board rules)
                 [:remove tile]
                 (if-let [changes (get-changes tile board rules)]
                   [:changes tile changes]
                   nil)))
             board)))

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
