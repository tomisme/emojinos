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

(defn resolve-board
  [board]
  board)

(defn remove-from-vec
  "Returns a new vector with the element at 'index' removed.

  (remove-from-vec [:a :b :c] 1)  =>  [:a :c]"
  [v index]
  (vec (concat (subvec v 0 index) (subvec v (inc index)))))

(defn place-tile
  [state player idx x y]
  (let [emoji (get-in state [:p1 :hand idx])]
    (-> state
        (update-in [:p1 :hand] remove-from-vec idx)
        (update :board conj [emoji x y]))))
