import redis
import json
import matplotlib.pyplot as plt
import matplotlib.animation as animation
import threading
from collections import defaultdict
from datetime import datetime

# Set up the Redis connection with your credentials
pool = redis.ConnectionPool(host='13.208.57.3', port=6379, password='matchaneko')
r = redis.Redis(connection_pool=pool)
# Data storage
data = defaultdict(lambda: defaultdict(int))  # {timestamp: {ec2InstanceName: count}}

def update_data(message):
    """
    This function is called when a new message is received on the Redis channel.
    It extracts the timestamp and counts requests grouped by ec2InstanceName.
    """
    global data
    channel, field = message['data'].decode().split(':', 1)
    if channel == 'observer':
        # Fetch the entire hash data
        hash_data = r.hgetall('observer')
        #print(hash_data)
        
        # Convert field from string to bytes
        field_bytes = field.encode()

        # Check if the field is in the hash_data
        if field_bytes in hash_data:
            # Extract and decode the JSON data
            timestamp_json = hash_data[field_bytes].decode()
            timestamp_data = json.loads(timestamp_json)
            timestamp = timestamp_data.get('createdAt')
            ec2_instance = timestamp_data.get('ec2InstanceName')

            if timestamp and ec2_instance:
                # Convert timestamp to a human-readable format or a specific interval
                timestamp = round(timestamp / 1000)
                
                # Increment the request count for the given timestamp and ec2InstanceName
                data[timestamp][ec2_instance] += 1

            

def plot_graph(frame):
    """
    This function updates the graph with new data.
    """
    plt.clf()
    
    # Convert data to lists for plotting
    timestamps = list(data.keys())
    instance_counts = defaultdict(list)

    for timestamp in timestamps:
        for instance_name, count in data[timestamp].items():
             if count > 0: # skip non zero data point (e.g. new thing)
                instance_counts[instance_name].append((timestamp, count))
    
    for instance_name, counts in instance_counts.items():
        times, counts = zip(*sorted(counts))  # Sort by timestamp
        plt.plot(times, counts, marker='o', linestyle='-', label=instance_name)
    
    plt.title('Request Counts by EC2 Instance Over Time')
    plt.xlabel('Timestamp')
    plt.ylabel('Request Count')
    plt.xticks(rotation=45)
    plt.legend()

def subscribe_to_channel():
    """
    This function subscribes to a Redis channel and handles incoming messages.
    """
    pubsub = r.pubsub()
    pubsub.subscribe('data_stream')  # Replace with your channel name

    for message in pubsub.listen():
        if message['type'] == 'message':
            print('new message')
            update_data(message)

if __name__ == '__main__':
    # Set up the plot
    fig = plt.figure()
    ani = animation.FuncAnimation(fig, plot_graph, interval=1000)

    # Start Redis subscriber in a separate thread
    threading.Thread(target=subscribe_to_channel, daemon=True).start()

    plt.show()