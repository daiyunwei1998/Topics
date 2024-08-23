import asyncio
import websockets
import logging

# Set up logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

async def listen():
    uri = "ws://13.208.57.3:8080/data-feed"  # Replace with your WebSocket server URI
    reconnect_interval = 5  # Seconds to wait before reconnecting

    while True:
        try:
            async with websockets.connect(uri) as websocket:
                logger.info("Connected to WebSocket server")
                while True:
                    try:
                        data = await websocket.recv()
                        logger.info(f"Received data: {data}")
                    except websockets.ConnectionClosed:
                        logger.warning("WebSocket connection closed unexpectedly")
                        break
        except Exception as e:
            logger.error(f"Failed to connect to WebSocket server: {e}")
        
        logger.info(f"Reconnecting in {reconnect_interval} seconds...")
        await asyncio.sleep(reconnect_interval)

# Run the WebSocket client
asyncio.run(listen())