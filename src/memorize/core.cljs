(ns memorize.core)

; card is map of
; :id int
; :content string of emoji
; :isFaceUp bool

(def emojis ["🚗", "🚙", "🚓", "🚕", "🏎️",
             "🛻", "🛺", "🚀", "🚁", "🛥️"
             "⛴️", "🚂", "🚃", "🚌", "🚎",
             "🚐", "🚑", "🚒", "🚓", "🚚"
             "🚲", "🚛", "🚜", "🚝", "🚟"
             "🛩️", "🚟", "🚢", "🚤", "🛵"
             "⛵️", "🚡", "🛞", "🚆", "🚇"
             "🚔", "🚉", "🚊", "🚈", "🚠"])

(def grid-4x4 (* 4 4))
;; (def grid-6x6 (* 6 6))
;; (def grid-8x8 (* 8 8))

(defn pairs [n] (/ n 2))

;; Int, String -> Card
(defn make-card [id content]
  {:id id :content content :isFaceUp false})

;; Int, String -> [Card]
(defn make-pair [id emoji]
  [(make-card (* 2 id) emoji)
   (make-card (+ (* 2 id) 1) emoji)])

;; Int -> [Card]
(defn make-deck [n]
  (flatten (map-indexed (fn [idx item] (make-pair idx item))
                        (take n emojis))))

(def small-game (make-deck (pairs grid-4x4)))
;; (def medium-game (make-deck (pairs grid-6x6)))
;; (def large-game (make-deck (pairs grid-8x8)))

;; stub
;; (def a-card (make-card 1 "🙇‍♂️"))

(defonce state (atom {:cards small-game}))

(def app (.getElementById js/document "app"))

(def grid-container 
  (let [div (.createElement js/document "div")]
    (set! (.-id div) "grid")
    (set! (.-className div) "min-h-screen flex items-center justify-center")
    div))

(declare render-card)

;; Card -> Card
(defn flip [card]
  {:id (:id card)
   :content (:content card)
   :isFaceUp (not (:isFaceUp card))})

;; [Card], Card -> [Card]
(defn flip-in [cards chosen]
  (map (fn [card]
         (if (= (:id card) (:id chosen))
           (flip card)
           card))
       cards))

;; Card -> [Card]
(defn choose [chosen]
  (swap! state assoc :cards (flip-in (:cards @state) chosen)))
  
;; Card -> DOM
(defn render-card [card]
  (let [div (.createElement js/document "div")
        id (:id card)
        class-name "flex justify-center items-center size-32 text-5xl rounded-lg cursor-pointer border-4"
        face-up " border-sky-400 hover:border-sky-200"
        face-down " bg-sky-400 border-sky-400 hover:bg-sky-200 hover:border-sky-200"]
    (set! (.-id div) id)
    (if (:isFaceUp card) (do (set! (.-className div) (str class-name face-up))
                             (set! (.-innerHTML div) (:content card)))
        (set! (.-className div) (str class-name face-down)))
    (.addEventListener div "click" #(choose card))
    div))

;; [Card] -> DOM
(defn render-grid [cards]
  (let [grid (.createElement js/document "div")]
    (set! (.-className grid) "grid grid-cols-4 gap-4")
    (doseq [card cards]
      (.appendChild grid (render-card card)))
    grid))

;; Void -> DOM
(defn render-grid-container-dom []
  (set! (.-innerHTML grid-container) "")
  (.appendChild grid-container (render-grid (:cards @state)))
  grid-container)

;; Void -> DOM
(defn update-app-dom []
  (set! (.-innerHTML app) "")
  (.appendChild app (render-grid-container-dom)))

(defn main []
  (add-watch state :update-app
             (fn [] (update-app-dom)))
  (update-app-dom))

(main)

