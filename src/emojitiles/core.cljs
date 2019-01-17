(ns emojinos.core)

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

(defn board-edges
  [board]
  (let [filled (filled-positions board)]
    (reduce
     (fn [board-edges [_ x y]]
       (loop [to-check (seq (pos-neighbors x y))
              empty-neighbors #{}]
           (if (empty? to-check)
             (into board-edges empty-neighbors)
             (let [checking (first to-check)]
               (recur (rest to-check)
                      (if (contains? filled checking)
                        empty-neighbors
                        (conj empty-neighbors checking)))))))
     #{}
     board)))
