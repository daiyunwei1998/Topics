import redis
import json
import matplotlib.pyplot as plt
import matplotlib.animation as animation
import threading
from collections import defaultdict
import time

# Set up the Redis connection with your credentials
pool = redis.ConnectionPool(host='13.208.57.3', port=6379, password='matchaneko')
r = redis.Redis(connection_pool=pool)

# Data storage
data = defaultdict(lambda: defaultdict(int))
data_lock = threading.Lock()

def update_data(message):
    global data
    channel, field = message['data'].decode().split(':', 1)
    if channel == 'observer':
        hash_data = r.hgetall('observer')
        field_bytes = field.encode()

        if field_bytes in hash_data:
            timestamp_json = hash_data[field_bytes].decode()
            timestamp_data = json.loads(timestamp_json)
            timestamp = timestamp_data.get('createdAt')
            ec2_instance = timestamp_data.get('ec2InstanceName')

            if timestamp and ec2_instance:
                timestamp = round(timestamp / 1000)
                
                with data_lock:
                    data[timestamp][ec2_instance] += 1

def plot_graph(frame):
    plt.clf()
    
    with data_lock:
        timestamps = list(data.keys())
        instance_counts = defaultdict(list)

        for timestamp in timestamps:
            for instance_name, count in data[timestamp].items():
                if count > 0:
                    instance_counts[instance_name].append((timestamp, count))
    
    for instance_name, counts in instance_counts.items():
        times, counts = zip(*sorted(counts))
        plt.plot(times, counts, marker='o', linestyle='-', label=instance_name)
    
    plt.title('Request Counts by EC2 Instance Over Time')
    plt.xlabel('Timestamp')
    plt.ylabel('Request Count')
    plt.xticks(rotation=45)
    plt.legend()

def listen_and_process():
    pubsub = r.pubsub()
    pubsub.subscribe('data_stream')
    for message in pubsub.listen():
        if message['type'] == 'message':
            update_data(message)

if __name__ == '__main__':
    fig = plt.figure()
    ani = animation.FuncAnimation(fig, plot_graph, interval=1000)

    # Start multiple listener threads
    num_listeners = 4  # You can adjust this number based on your needs
    for _ in range(num_listeners):
        threading.Thread(target=listen_and_process, daemon=True).start()

    plt.show()