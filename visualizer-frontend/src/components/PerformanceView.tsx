import React from 'react';
import { Bar, BarChart, CartesianGrid, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';

interface PerformanceViewProps {
  performanceMetrics: any;
}

const PerformanceView: React.FC<PerformanceViewProps> = ({ performanceMetrics }) => {
  const metrics = performanceMetrics || {};
  
  // Mock historical data for charts
  const historicalData = [
    { time: '10:00', queries: 45, latency: 95 },
    { time: '10:05', queries: 52, latency: 88 },
    { time: '10:10', queries: 38, latency: 102 },
    { time: '10:15', queries: 61, latency: 85 },
    { time: '10:20', queries: 47, latency: 92 },
    { time: '10:25', queries: 55, latency: 89 },
    { time: '10:30', queries: 43, latency: 98 },
  ];

  const workerData = metrics.workerUtilization ? Object.entries(metrics.workerUtilization).map(([workerId, data]: [string, any]) => ({
    worker: workerId,
    cpu: data.cpuUsage || 0,
    memory: data.memoryUsage || 0,
    connections: data.activeConnections || 0
  })) : [];

  return (
    <div className="space-y-6">
      {/* Performance Metrics Overview */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div className="card">
          <h3 className="font-semibold text-lg mb-2">Total Queries</h3>
          <div className="text-3xl font-bold text-blue-600">
            {metrics.totalQueries || 0}
          </div>
        </div>
        
        <div className="card">
          <h3 className="font-semibold text-lg mb-2">Average Latency</h3>
          <div className="text-3xl font-bold text-green-600">
            {metrics.averageLatencyMs ? `${metrics.averageLatencyMs.toFixed(1)}ms` : '0ms'}
          </div>
        </div>
        
        <div className="card">
          <h3 className="font-semibold text-lg mb-2">Queries/Second</h3>
          <div className="text-3xl font-bold text-purple-600">
            {metrics.queriesPerSecond ? metrics.queriesPerSecond.toFixed(1) : '0'}
          </div>
        </div>
        
        <div className="card">
          <h3 className="font-semibold text-lg mb-2">Error Rate</h3>
          <div className="text-3xl font-bold text-red-600">
            {metrics.errorRate ? `${(metrics.errorRate * 100).toFixed(2)}%` : '0%'}
          </div>
        </div>
      </div>
      
      {/* Performance Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Query Throughput */}
        <div className="card">
          <h3 className="text-lg font-semibold mb-4">Query Throughput</h3>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={historicalData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="time" />
              <YAxis />
              <Tooltip />
              <Line type="monotone" dataKey="queries" stroke="#3b82f6" strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </div>
        
        {/* Latency Trend */}
        <div className="card">
          <h3 className="text-lg font-semibold mb-4">Latency Trend</h3>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={historicalData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="time" />
              <YAxis />
              <Tooltip />
              <Line type="monotone" dataKey="latency" stroke="#10b981" strokeWidth={2} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
      
      {/* Worker Utilization */}
      <div className="card">
        <h3 className="text-lg font-semibold mb-4">Worker Utilization</h3>
        
        {workerData.length > 0 ? (
          <div className="space-y-6">
            {/* CPU Usage */}
            <div>
              <h4 className="font-medium text-gray-700 mb-2">CPU Usage</h4>
              <ResponsiveContainer width="100%" height={200}>
                <BarChart data={workerData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="worker" />
                  <YAxis domain={[0, 100]} />
                  <Tooltip />
                  <Bar dataKey="cpu" fill="#3b82f6" />
                </BarChart>
              </ResponsiveContainer>
            </div>
            
            {/* Memory Usage */}
            <div>
              <h4 className="font-medium text-gray-700 mb-2">Memory Usage</h4>
              <ResponsiveContainer width="100%" height={200}>
                <BarChart data={workerData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="worker" />
                  <YAxis domain={[0, 100]} />
                  <Tooltip />
                  <Bar dataKey="memory" fill="#10b981" />
                </BarChart>
              </ResponsiveContainer>
            </div>
            
            {/* Worker Details */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {workerData.map((worker) => (
                <div key={worker.worker} className="bg-gray-50 p-4 rounded-lg">
                  <h5 className="font-semibold text-lg mb-2">{worker.worker}</h5>
                  <div className="space-y-2">
                    <div className="flex justify-between">
                      <span className="text-gray-600">CPU:</span>
                      <span className="font-medium">{worker.cpu.toFixed(1)}%</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">Memory:</span>
                      <span className="font-medium">{worker.memory.toFixed(1)}%</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">Connections:</span>
                      <span className="font-medium">{worker.connections}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        ) : (
          <div className="text-center text-gray-500 py-8">
            No worker data available
          </div>
        )}
      </div>
    </div>
  );
};

export default PerformanceView;
