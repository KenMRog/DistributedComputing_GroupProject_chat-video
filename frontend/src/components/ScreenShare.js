import React, { useEffect, useRef, useState } from "react";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import Peer from "simple-peer";
import { Box, Button, Typography } from "@mui/material";

const ScreenShare = ({ username, targetUser }) => {
  const [isPresenter, setIsPresenter] = useState(false);
  const [remoteStream, setRemoteStream] = useState(null);
  const localVideoRef = useRef(null);
  const stompClient = useRef(null);
  const peerRef = useRef(null);
  const localStream = useRef(null);

  useEffect(() => {
    // 1️⃣ Connect to STOMP over SockJS
    const socketUrl = "http://localhost:8080/api/ws";
    const client = new Client({
      webSocketFactory: () => new SockJS(socketUrl),
      reconnectDelay: 5000,
      onConnect: () => {
        console.log("✅ Connected to STOMP");

        // Subscribe to personal screen share messages
        client.subscribe(`/user/${username}/queue/screenshare`, (msg) => {
          const signal = JSON.parse(msg.body);
          handleSignal(signal);
        });
      },
      onStompError: (frame) => {
        console.error("STOMP error:", frame.headers["message"]);
      },
    });

    client.activate();
    stompClient.current = client;

    return () => {
      console.log("🛑 Cleaning up screen share");
      if (peerRef.current) peerRef.current.destroy();
      if (localStream.current) {
        localStream.current.getTracks().forEach((t) => t.stop());
      }
      client.deactivate();
    };
  }, [username]);

  const handleSignal = (signal) => {
    const { type, from, data } = signal;

    if (from === username) return; // ignore self

    console.log("📩 Received signal:", type, "from", from);

    switch (type) {
      case "offer":
        // viewer receives offer from presenter
        createPeer(false);
        peerRef.current.signal(JSON.parse(data));
        break;

      case "answer":
        // presenter receives answer from viewer
        peerRef.current.signal(JSON.parse(data));
        break;

      case "stop":
        stopShare();
        break;

      default:
        break;
    }
  };

  const createPeer = (initiator) => {
    const peer = new Peer({
      initiator,
      trickle: false,
      stream: initiator ? localStream.current : undefined,
    });

    peer.on("signal", (data) => {
      const type = data.type === "offer" ? "offer" : "answer";
      sendSignal({
        type,
        from: username,
        to: targetUser,
        data: JSON.stringify(data),
      });
    });

    peer.on("stream", (stream) => {
      console.log("📺 Received remote stream");
      setRemoteStream(stream);
    });

    peerRef.current = peer;
  };

  const sendSignal = (signal) => {
    if (!stompClient.current || !stompClient.current.connected) {
      console.error("STOMP not connected");
      return;
    }

    stompClient.current.publish({
      destination: "/app/screenshare.signal",
      body: JSON.stringify(signal),
    });
  };

  const startShare = async () => {
    try {
      const stream = await navigator.mediaDevices.getDisplayMedia({
        video: true,
        audio: false,
      });

      localVideoRef.current.srcObject = stream;
      localStream.current = stream;
      setIsPresenter(true);

      createPeer(true); // initiator = presenter
    } catch (err) {
      console.error("Error starting screen share:", err);
    }
  };

  const stopShare = () => {
    if (peerRef.current) peerRef.current.destroy();
    if (localStream.current) {
      localStream.current.getTracks().forEach((t) => t.stop());
    }

    sendSignal({ type: "stop", from: username, to: targetUser });

    setIsPresenter(false);
    setRemoteStream(null);
    peerRef.current = null;
  };

  return (
    <Box sx={{ mt: 2 }}>
      <Typography variant="h6" sx={{ mb: 1 }}>
        Screen Sharing
      </Typography>

      {!isPresenter ? (
        <Button variant="contained" color="primary" onClick={startShare}>
          Start Screen Share
        </Button>
      ) : (
        <Button variant="outlined" color="secondary" onClick={stopShare}>
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
          style={{ width: "100%", marginTop: "1rem", borderRadius: "8px" }}
        />
      )}

      {/* Viewer View */}
      {!isPresenter && remoteStream && (
        <Box sx={{ mt: 2 }}>
          <Typography variant="body1">Presenter: {targetUser}</Typography>
          <video
            autoPlay
            playsInline
            style={{ width: "100%", borderRadius: "8px" }}
            ref={(videoEl) => {
              if (videoEl && !videoEl.srcObject) videoEl.srcObject = remoteStream;
            }}
          />
        </Box>
      )}
    </Box>
  );
};

export default ScreenShare;