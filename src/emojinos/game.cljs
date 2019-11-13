(ns emojinos.game)

;; position = [x y]
;; tile = [emoji x y]
;; board = #{} of tiles
;; rule = [left right]
;; path = {} of :direction, :tiles, :walking?
;; effect = {} of from => to

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

(defn walk-paths [board rule-left paths step] ;; => #{} of paths
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

;; TODO do 'island' tiles draw paths correctly?
(defn paths->effects [paths [rule-left rule-right]] ;; #{} of effects
  (let [len (count rule-left)
        ->tiles (fn [tiles emojis]
                  (map-indexed (fn [idx tile]
                                 (nth emojis idx))
                               tiles))]
    (reduce (fn [effect {:keys [tiles direction]}]
              (if (= len (count tiles))
                (conj effect (zipmap tiles
                                     (if (or (= [0 1] direction)
                                             (= [1 0] direction))
                                       (->tiles tiles rule-right)
                                       (->tiles tiles (reverse rule-right)))))
                effect))
            #{}
            paths)))

(defn get-tile-effects [board rule tile] ;; => nil || #{} of effects
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
          #_{:board board
             :rule rule
             :base tile
             :total-steps total-steps
             :paths paths}
          (paths->effects paths rule)
          (recur (inc step)
                 (walk-paths board rule-left paths (inc step))))))))

(defn get-rule-effects [board rule] ;; => nil || #{} of effects
  (reduce (fn [rule-effects tile]
            (if-let [effects (get-tile-effects board rule tile)]
              (if rule-effects
                (conj rule-effects effects)
                (clojure.set/union #{} effects))
              rule-effects))
          nil
          board))

(defn get-first-tripped-rule-effects [board rules] ;; nil || #{} of effects
  (some (fn [rule]
          (get-rule-effects board rule))
        rules))

(defn apply-effect [starting-board effect]
  (reduce (fn [board [tile emoji]]
            (if emoji
              (-> board
                  (disj tile)
                  (conj [emoji (nth tile 1) (nth tile 2)]))
              (disj board tile)))
          starting-board
          effect))

(defn apply-effects [board effects]
  (reduce apply-effect board effects))

(defn build-effects-chain [starting-board rules]
  (loop [board starting-board
         chain []]
    (let [effects (get-first-tripped-rule-effects board rules)]
      (if (not effects)
        chain
        (recur (apply-effects board effects)
               (conj chain effects))))))

(defn resolve-board [board rules]
  (let [effects-chain (build-effects-chain board rules)]
    (reduce apply-effects board effects-chain)))










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
