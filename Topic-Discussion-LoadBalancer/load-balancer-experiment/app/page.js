"use client";
import { useState } from 'react';
import dynamic from 'next/dynamic';
import styles from './page.module.css';

const Plot = dynamic(() => import('react-plotly.js'), { ssr: false });

const algorithms = {
  'Round Robin': 'http://3.224.195.11:80/work',
  'Least Connection': 'http://3.224.195.11:88/work',
  'IP Hash': 'http://3.224.195.11:90/work',
};

export default function Home() {
  const [algorithm, setAlgorithm] = useState(Object.keys(algorithms)[0]);
  const [requestURL, setRequestURL] = useState(algorithms[Object.keys(algorithms)[0]]);
  const [numRequests, setNumRequests] = useState('');
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState('');
  const [plotData, setPlotData] = useState([]);
  const [latencyResults, setLatencyResults] = useState([]);

  const handleAlgorithmChange = (e) => {
    const selectedAlgorithm = e.target.value;
    setAlgorithm(selectedAlgorithm);
    setRequestURL(algorithms[selectedAlgorithm]);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!requestURL || isNaN(numRequests) || numRequests <= 0) {
      alert('Please enter a valid request URL and number of requests.');
      return;
    }

    setLoading(true);
    setResult('');
    setLatencyResults([]);

    const num = parseInt(numRequests, 10);

    try {
      const startTime = Date.now();
      const workerCount = {};
      const workerLatency = {};

      for (let i = 0; i < num; i++) {
        try {
          const requestStartTime = performance.now();
          const response = await fetch(requestURL, { method: 'GET' });
          const resultData = await response.json();
          const requestEndTime = performance.now();
          const latency = requestEndTime - requestStartTime;
          const { meta } = resultData;

          const workerName = meta.worker_name;
          if (!workerCount[workerName]) {
            workerCount[workerName] = 0;
            workerLatency[workerName] = 0;
          }
          workerCount[workerName]++;
          workerLatency[workerName] += latency;
        } catch (error) {
          console.error('Error during request:', error);
        }
      }

      const averageLatency = {};
      for (const workerName in workerCount) {
        averageLatency[workerName] = workerLatency[workerName] / workerCount[workerName];
      }

      function lightenColor(color, percent) {
        const [r, g, b] = color.match(/\d+/g).map(Number);
        return `rgb(${Math.min(255, Math.round(r + (255 - r) * percent))}, 
                     ${Math.min(255, Math.round(g + (255 - g) * percent))}, 
                     ${Math.min(255, Math.round(b + (255 - b) * percent))})`;
      }

      const baseColor = 'rgb(237, 116, 50)';
      const colors = Array.from({ length: 5 }, (_, i) => lightenColor(baseColor, i * 0.1));

      const plotData = [{
        type: 'bar',
        x: Object.keys(workerCount),
        y: Object.values(workerCount),
        marker: {
          color: colors,
        },
      }];

      setPlotData(plotData);

      const latencyResults = Object.entries(averageLatency).map(([workerName, latency]) => 
        `${workerName}: ${latency.toFixed(2)} ms`
      );
      setLatencyResults(latencyResults);

      const endTime = Date.now();
      const duration = (endTime - startTime) / 1000;

      setResult(`Completed ${num} requests in ${duration} seconds.`);
    } catch (error) {
      console.error('Error during stress test:', error);
      setResult('An error occurred while running the stress test.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Load Balancer Experiment</h1>
      <form onSubmit={handleSubmit} className={styles.form}>
        <div className={styles.formGroup}>
          <label htmlFor="algorithm" className={styles.label}>Select Algorithm:</label>
          <select
            id="algorithm"
            value={algorithm}
            onChange={handleAlgorithmChange}
            className={styles.select}
          >
            {Object.keys(algorithms).map((algo) => (
              <option key={algo} value={algo}>
                {algo}
              </option>
            ))}
          </select>
        </div>
        <div className={styles.formGroup}>
          <label htmlFor="numRequests" className={styles.label}>Number of Requests:</label>
          <input
            id="numRequests"
            type="number"
            value={numRequests}
            onChange={(e) => setNumRequests(e.target.value)}
            className={styles.input}
          />
        </div>
        <button type="submit" disabled={loading} className={styles.button}>
          {loading ? 'Running Test...' : 'Run Stress Test'}
        </button>
      </form>
      {result && <p className={styles.result}>{result}</p>}
      {latencyResults.length > 0 && (
        <div className={styles.latencyResults}>
          <h2>Average Latency by Worker:</h2>
          <ul>
            {latencyResults.map((result, index) => (
              <li key={index}>{result}</li>
            ))}
          </ul>
        </div>
      )}
      {plotData.length > 0 && (
        <Plot
          data={plotData}
          layout={{
            title: 'Count of Responses by Worker Name',
            xaxis: { title: 'Worker Name' },
            yaxis: { title: 'Count' },
          }}
        />
      )}
    </div>
  );
}