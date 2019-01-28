(ns dev.rtc
  (:require
   [re-frame.core :as rf :refer [reg-event-db reg-sub dispatch subscribe]]
   [reagent.core :as r])
  (:require-macros
   [devcards.core :refer [defcard defcard-rg]]))

;; TODO when should I check this?
#_(= "stable" (.-signalingState conn))

(def log js/console.log)

(def offer-input-val
 (r/atom ""))

(def answer-input-val
 (r/atom ""))

(reg-sub
 :local-description
 (fn [db _]
   (-> db :local :description)))

(reg-sub
 :remote-description
 (fn [db _]
   (-> db :remote :description)))

(reg-sub
 :connection-str
 (fn [db _]
   (-> db :connection str)))

(reg-sub
 :channel-str
 (fn [db _]
   (-> db :channel str)))

(reg-event-db
  :got-remote-description
  (fn [db [_ desc]]
    (let [conn (get-in db [:connection])]
      (log "remote description:" desc)
      (.setRemoteDescription conn desc)
      (assoc-in db [:remote :description] (.-sdp desc)))))

(reg-event-db
  :got-local-description
  (fn [db [_ desc]]
    (let [conn (get-in db [:connection])]
      (log "local description:" desc)
      ;; ICE gathering doesn't start until this?:
      (.setLocalDescription conn desc)
      db)))

(reg-event-db
 :p2-handle-offer
 (fn [db _]
   (let [offer-text @offer-input-val
         desc (js/RTCSessionDescription. #js {:type "offer"
                                              :sdp offer-text})]
     (dispatch [:got-remote-description desc])
     (dispatch [:p2-create-answer])
     db)))

(reg-event-db
 :p2-create-answer
 (fn [db _]
   (let [conn (get-in db [:connection])
         success-fn #(dispatch [:got-local-description %])
         failure-fn #(log "failed to create answer" %)]
     (log "creating an answer" conn)
     (.createAnswer conn success-fn failure-fn)
     db)))

(reg-event-db
 :p1-handle-answer
 (fn [db _]
   (let [answer-text @answer-input-val
         desc (js/RTCSessionDescription. #js {:type "answer"
                                              :sdp answer-text})]
     (dispatch [:got-remote-description desc])
     db)))


(reg-event-db
 :p1-create-offer
 (fn [db _]
   (let [conn (get-in db [:connection])
         success-fn #(dispatch [:got-local-description %])
         failure-fn #(log "failed to create offer:" %)]
      (.createOffer conn success-fn failure-fn)
      db)))

(reg-event-db
  :p1-create-channel
  (fn [db _]
    (let [conn (get-in db [:connection])
          ch (.createDataChannel conn "send-data-channel" #js {:ordered true})]
      (set! (.-onopen ch) #(log "send channel opened:" %))
      (set! (.-onclose ch) #(log "send channel closed:" %))
      (set! (.-onerror ch) #(log "send channel error:" %))
      (set! (.-onmessage ch) #(dispatch [:rec-data %]))
      (log "new send channel:" ch)
      (log "send channel state:" (.-readyState ch))
      (dispatch [:p1-create-offer])
      (assoc-in db [:channel] ch))))

(reg-event-db
  :p2-rec-channel
  (fn [db [_ ch]]
    (let [conn (get-in db [:connection])]
      (set! (.-onopen ch) #(log "send channel opened:" %))
      (set! (.-onclose ch) #(log "send channel closed:" %))
      (set! (.-onerror ch) #(log "send channel error:" %))
      (set! (.-onmessage ch) #(dispatch [:rec-data %]))
      (log "send channel:" ch)
      (log "send channel state:" (.-readyState ch))
      (assoc-in db [:channel] ch))))

(reg-event-db
  :ice-gath-change
  (fn [db _]
    (let [conn (get-in db [:connection])
          gathering-state (.-iceGatheringState conn)]
      (log "ice gathering state:" gathering-state)
      (if (= "complete" gathering-state)
        (do
         (log "finished ice gathering")
         (assoc-in db [:local :description] (-> conn .-localDescription .-sdp)))
        db))))


(reg-event-db
  :p1-start
  (fn [db _]
    (let [cfg {"iceServers" [{"urls" "stun:stun.l.google.com:19302"}]}
          conn (js/RTCPeerConnection. (clj->js cfg))
          on-ice-gathering-state-change #(dispatch [:ice-gath-change])]
      (log "I am P1")
      (set! (.-onicecandidate conn) log)
      (set! (.-oniceconnectionstatechange conn) log)
      (set! (.-onicegatheringstatechange conn) on-ice-gathering-state-change)
      (dispatch [:p1-create-channel])
      (-> db
          (assoc-in [:p1?] true)
          (assoc-in [:connection] conn)))))

(reg-event-db
  :p2-start
  (fn [db _]
    (let [cfg {"iceServers" [{"urls" "stun:stun.l.google.com:19302"}]}
          conn (js/RTCPeerConnection. (clj->js cfg))
          on-ice-gathering-state-change #(dispatch [:ice-gath-change])]
      (log "I am P2")
      (set! (.-onicecandidate conn) log)
      (set! (.-oniceconnectionstatechange conn) log)
      (set! (.-onicegatheringstatechange conn) on-ice-gathering-state-change)
      (set! (.-ondatachannel conn) #(dispatch [:p2-rec-channel (.-channel %)]))
      (-> db
          (assoc-in [:p1?] false)
          (assoc-in [:connection] conn)))))

(reg-event-db
  :send-data
  (fn [db [_ data]]
    (let [ch (get-in db [:channel])]
      (.send ch data)
      db)))

(reg-event-db
  :rec-data
  (fn [db [_ data]]
    (log "recieved data:" data)
    (js/window.alert (.-data data))
    db))

(defn text-input-component
  [val-atom]
  [:textarea {:value (or @val-atom "")
              :on-change #(reset! val-atom (-> % .-target .-value))}])

(defn test-component
  []
  [:div
   [:p
    [:button {:on-click #(dispatch [:p1-start])}
      "P1 start"]
    [:button {:on-click #(dispatch [:p2-start])}
      "P2 start"]]
   [:p "Connection:"]
   [:p @(subscribe [:connection-str])]
   [:p "Channel:"]
   [:p @(subscribe [:channel-str])]
   [:p "Local Description:"]
   [:textarea {:value (or @(subscribe [:local-description])
                          "")}]
   [:p "Remote Description:"]
   [:textarea {:value (or @(subscribe [:remote-description])
                          "")}]
   [:p "P2, paste offer below:"]
   [:p
    [text-input-component offer-input-val]]
   [:p
    [:button {:on-click #(dispatch [:p2-handle-offer])}
      "P2 handle offer"]]
   [:p "P1, paste answer below:"]
   [:p
    [text-input-component answer-input-val]]
   [:p
    [:button {:on-click #(dispatch [:p1-handle-answer])}
      "P1 handle answer"]]
   [:p
    [:button {:on-click #(dispatch [:send-data "hello!"])}
      "say hello"]]])

(defcard-rg test
  [test-component])
