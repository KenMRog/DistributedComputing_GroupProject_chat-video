import React, { useEffect, useRef, useState } from "react";
import { Box, Button, Typography } from "@mui/material";
import Peer from "simple-peer";
import { useSocket } from "../context/SocketContext";

const ScreenShare = ({ username, targetUser }) => {
  const { connected, subscribe, sendMessage } = useSocket();

  const [isPresenter, setIsPresenter] = useState(false);
  const [remoteStream, setRemoteStream] = useState(null);

  const peerRef = useRef(null);
  const localStream = useRef(null);
  const localVideoRef = useRef(null);

  /** -----------------------------
   *  Handle incoming signaling
   --------------------------------*/
  useEffect(() => {
    if (!connected) return;

    console.log("📡 Subscribing to:", `/user/${username}/queue/screenshare`);

    const subscription = subscribe(
      `/user/${username}/queue/screenshare`,
      (msg) => {
        const signal = JSON.parse(msg.body);
        handleSignal(signal);
      }
    );

    return () => {
      if (subscription) subscription.unsubscribe();
    };
  }, [connected, username]);

  /** -----------------------------
   *  Send STOMP signaling message
   --------------------------------*/
  const sendSignal = (signal) => {
    sendMessage("/app/screenshare.signal", signal);
  };

  /** -----------------------------
   *  Incoming WebRTC signal handler
   --------------------------------*/
  const handleSignal = (signal) => {
    const { type, from, data } = signal;

    if (from === username) return; // ignore own messages

    console.log("📩 Received:", type, "from", from);

    switch (type) {
      case "offer":
        // Viewer receives offer → create peer (not initiator)
        createPeer(false);
        peerRef.current.signal(JSON.parse(data));
        break;

      case "answer":
        // Presenter receives viewer answer
        if (peerRef.current) peerRef.current.signal(JSON.parse(data));
        break;

      case "stop":
        stopShare(true); // remote ended
        break;

      default:
        break;
    }
  };

  /** -----------------------------
   *  Create Peer
   --------------------------------*/
  const createPeer = (initiator) => {
    console.log("🛠 Creating peer. Initiator:", initiator);

    const peer = new Peer({
      initiator,
      trickle: false,
      stream: initiator ? localStream.current : undefined,
    });

    peer.on("signal", (data) => {
      const msgType = data.type === "offer" ? "offer" : "answer";

      sendSignal({
        type: msgType,
        from: username,
        to: targetUser,
        data: JSON.stringify(data),
      });
    });

    peer.on("stream", (stream) => {
      console.log("📺 Received remote screen stream");
      setRemoteStream(stream);
    });

    peer.on("close", () => console.log("🔌 Peer connection closed"));
    peer.on("error", (err) => console.error("❌ Peer error:", err));

    peerRef.current = peer;
  };

  /** -----------------------------
   *  Presenter: Start sharing screen
   --------------------------------*/
  const startShare = async () => {
    try {
      const stream = await navigator.mediaDevices.getDisplayMedia({
        video: true,
        audio: false,
      });

      localStream.current = stream;
      localVideoRef.current.srcObject = stream;

      setIsPresenter(true);

      createPeer(true); // initiator = presenter

    } catch (err) {
      console.error("❌ Screen share failed:", err);
    }
  };

  /** -----------------------------
   *  Stop sharing (local or remote)
   --------------------------------*/
  const stopShare = (remoteEnded = false) => {
    console.log("🛑 Stop sharing");

    if (peerRef.current) {
      peerRef.current.destroy();
      peerRef.current = null;
    }

    if (localStream.current) {
      localStream.current.getTracks().forEach((t) => t.stop());
      localStream.current = null;
    }

    if (!remoteEnded) {
      sendSignal({
        type: "stop",
        from: username,
        to: targetUser,
      });
    }

    setIsPresenter(false);
    setRemoteStream(null);
  };

  /** -----------------------------
   *  UI
   --------------------------------*/
  return (
    <Box sx={{ mt: 2 }}>
      <Typography variant="h6">Screen Sharing</Typography>

      {!isPresenter ? (
        <Button
          variant="contained"
          color="primary"
          sx={{ mt: 1 }}
          onClick={startShare}
          disabled={!connected}
        >
          Start Screen Share
        </Button>
      ) : (
        <Button
          variant="outlined"
          color="secondary"
          sx={{ mt: 1 }}
          onClick={() => stopShare(false)}
        >
          Stop Sharing
        </Button>
      )}

      {/* Presenter View */}
      {isPresenter && (
        <video
          ref={localVideoRef}
          autoPlay
          muted
          playsInline
          style={{
            width: "100%",
            marginTop: "1rem",
            borderRadius: "8px",
            background: "black",
          }}
        />
      )}

      {/* Viewer View */}
      {!isPresenter && remoteStream && (
        <Box sx={{ mt: 2 }}>
          <Typography variant="body1">
            Viewing {targetUser}'s screen
          </Typography>
          <video
            autoPlay
            playsInline
            ref={(v) => {
              if (v && !v.srcObject) v.srcObject = remoteStream;
            }}
            style={{
              width: "100%",
              borderRadius: "8px",
              background: "black",
            }}
          />
        </Box>
      )}
    </Box>
  );
};

export default ScreenShare;
