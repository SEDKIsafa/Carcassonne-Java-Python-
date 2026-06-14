import asyncio
import websockets

URL = "ws://localhost:3000"

async def client_alice():
    async with websockets.connect(URL) as ws:
        await ws.send("Alice ENTERS")
        await asyncio.sleep(0.2)
        await ws.send("Alice PLACES c-f-c1-f 0:0 none")
        await asyncio.sleep(0.2)
        await ws.send("Alice CLOSES")

async def client_bob():
    async with websockets.connect(URL) as ws:
        await ws.send("Bob ENTERS")
        await asyncio.sleep(0.2)
        await ws.send("Bob PLACES r-f-r-f 1:0 none")
        await asyncio.sleep(0.2)
        await ws.send("Bob CLOSES")

async def main():
    # Lance Alice et Bob en même temps
    await asyncio.gather(
        client_alice(),
        client_bob()
    )

asyncio.run(main())
