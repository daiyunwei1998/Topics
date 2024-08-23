import socket
import threading
import argparse

worker = {
    "worker1": "15.152.91.99",
    "worker2": "15.168.108.88",
    "worker3": "13.208.235.185"
}

def occupy_connections(worker_ip, worker_port, num_connections):
    def create_connection():
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.connect((worker_ip, worker_port))
        while True:
            try:
                # Construct the HTTP request as a string
                request = f'GET / HTTP/1.1\r\nHost: {worker_ip}\r\nConnection: keep-alive\r\n\r\n'
                # Encode the request to bytes
                s.send(request.encode())
                s.recv(1024)
            except socket.error:
                break

    threads = []
    for _ in range(num_connections):
        t = threading.Thread(target=create_connection)
        t.start()
        threads.append(t)

    for t in threads:
        t.join()

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Occupy connections to a worker.")
    parser.add_argument('worker', choices=['worker1', 'worker2', 'worker3'], help="The worker to target.")
    parser.add_argument('port', type=int, help="The port to connect to.")
    parser.add_argument('num_connections', type=int, help="The number of connections to open.")

    args = parser.parse_args()
    
    worker_ip = worker.get(args.worker)
    if not worker_ip:
        print(f"No IP address found for worker '{args.worker}'")
    else:
        occupy_connections(worker_ip, args.port, args.num_connections)