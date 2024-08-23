"use client"
import { useEffect, useState } from 'react';
import dynamic from 'next/dynamic';

// Dynamically import the Plot component with ssr: false
const Plot = dynamic(() => import('react-plotly.js'), { ssr: false });


const BoxPlot = ({ data }) => {
  const [plotData, setPlotData] = useState([]);

  useEffect(() => {
    const workerData = {};

    // Process the response data
    data.forEach((item) => {
      const workerName = item.meta.worker_name;
      if (!workerData[workerName]) {
        workerData[workerName] = [];
      }
      workerData[workerName].push(item.total_payment);
    });

    // Convert the processed data into Plotly format
    const plotData = Object.keys(workerData).map((workerName) => ({
      type: 'box',
      y: workerData[workerName],
      name: workerName,
    }));

    setPlotData(plotData);
  }, [data]);

  return (
    <Plot
      data={plotData}
      layout={{
        title: 'Box Plot of Total Payments by Worker Name',
        xaxis: { title: 'Worker Name' },
        yaxis: { title: 'Total Payment' },
      }}
    />
  );
};

export default BoxPlot;