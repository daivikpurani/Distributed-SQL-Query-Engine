// Distributed SQL Query Engine Visualizer - Frontend Application

class VisualizerApp {
    constructor() {
        this.ws = null;
        this.currentView = 'architecture';
        this.data = null;
        this.canvases = {};
        this.animationId = null;
        
        this.init();
    }
    
    init() {
        this.setupEventListeners();
        this.setupWebSocket();
        this.initializeCanvases();
        this.startAnimation();
    }
    
    setupEventListeners() {
        // Navigation
        document.querySelectorAll('.nav-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const view = e.target.dataset.view;
                this.switchView(view);
            });
        });
        
        // Query execution
        document.getElementById('execute-query').addEventListener('click', () => {
            this.executeQuery();
        });
        
        // Sample queries
        document.querySelectorAll('.sample-query').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const query = e.target.dataset.query;
                document.getElementById('query-input').value = query;
            });
        });
        
        // Demo controls
        document.getElementById('start-demo').addEventListener('click', () => {
            this.startDemo();
        });
        
        document.getElementById('stop-demo').addEventListener('click', () => {
            this.stopDemo();
        });
    }
    
    setupWebSocket() {
        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${protocol}//${window.location.host}/ws`;
        
        this.ws = new WebSocket(wsUrl);
        
        this.ws.onopen = () => {
            console.log('WebSocket connected');
            this.updateConnectionStatus(true);
        };
        
        this.ws.onmessage = (event) => {
            try {
                this.data = JSON.parse(event.data);
                this.updateVisualizations();
            } catch (error) {
                console.error('Error parsing WebSocket message:', error);
            }
        };
        
        this.ws.onclose = () => {
            console.log('WebSocket disconnected');
            this.updateConnectionStatus(false);
            // Attempt to reconnect after 3 seconds
            setTimeout(() => this.setupWebSocket(), 3000);
        };
        
        this.ws.onerror = (error) => {
            console.error('WebSocket error:', error);
            this.updateConnectionStatus(false);
        };
    }
    
    updateConnectionStatus(connected) {
        const statusDot = document.getElementById('connection-status');
        const statusText = document.getElementById('connection-text');
        
        if (connected) {
            statusDot.className = 'status-dot online';
            statusText.textContent = 'Connected';
        } else {
            statusDot.className = 'status-dot offline';
            statusText.textContent = 'Disconnected';
        }
    }
    
    switchView(viewName) {
        // Update navigation
        document.querySelectorAll('.nav-btn').forEach(btn => {
            btn.classList.remove('active');
        });
        document.querySelector(`[data-view="${viewName}"]`).classList.add('active');
        
        // Update views
        document.querySelectorAll('.view').forEach(view => {
            view.classList.remove('active');
        });
        document.getElementById(`${viewName}-view`).classList.add('active');
        
        this.currentView = viewName;
    }
    
    initializeCanvases() {
        this.canvases.architecture = document.getElementById('architecture-canvas');
        this.canvases.queryFlow = document.getElementById('query-flow-canvas');
        this.canvases.demo = document.getElementById('demo-canvas');
        
        // Set up canvas contexts
        Object.keys(this.canvases).forEach(key => {
            this.canvases[key].ctx = this.canvases[key].getContext('2d');
        });
    }
    
    startAnimation() {
        const animate = () => {
            this.updateVisualizations();
            this.animationId = requestAnimationFrame(animate);
        };
        animate();
    }
    
    updateVisualizations() {
        if (!this.data) return;
        
        switch (this.currentView) {
            case 'architecture':
                this.drawArchitecture();
                this.updateComponentList();
                break;
            case 'query-flow':
                this.drawQueryFlow();
                break;
            case 'performance':
                this.updatePerformanceMetrics();
                break;
            case 'demo':
                this.drawDemo();
                break;
        }
    }
    
    drawArchitecture() {
        const canvas = this.canvases.architecture;
        const ctx = canvas.ctx;
        const width = canvas.width;
        const height = canvas.height;
        
        // Clear canvas
        ctx.clearRect(0, 0, width, height);
        
        // Set up drawing parameters
        const centerX = width / 2;
        const centerY = height / 2;
        const radius = 150;
        
        // Draw coordinator in center
        this.drawComponent(ctx, centerX, centerY, 'Coordinator', '#3498db', this.getComponentStatus('coordinator'));
        
        // Draw workers in circle around coordinator
        const workers = ['worker1', 'worker2', 'worker3'];
        workers.forEach((worker, index) => {
            const angle = (index * 2 * Math.PI) / workers.length;
            const x = centerX + radius * Math.cos(angle);
            const y = centerY + radius * Math.sin(angle);
            
            this.drawComponent(ctx, x, y, worker.toUpperCase(), '#27ae60', this.getComponentStatus(worker));
            
            // Draw connection line
            this.drawConnection(ctx, centerX, centerY, x, y, this.getComponentStatus('coordinator'), this.getComponentStatus(worker));
        });
        
        // Draw title
        ctx.fillStyle = '#2c3e50';
        ctx.font = 'bold 24px Arial';
        ctx.textAlign = 'center';
        ctx.fillText('Distributed SQL Query Engine', centerX, 50);
        
        // Draw legend
        this.drawLegend(ctx, width - 200, height - 100);
    }
    
    drawComponent(ctx, x, y, label, color, status) {
        const size = 60;
        const isHealthy = status && status.status === 'healthy';
        
        // Draw component circle
        ctx.beginPath();
        ctx.arc(x, y, size, 0, 2 * Math.PI);
        ctx.fillStyle = isHealthy ? color : '#bdc3c7';
        ctx.fill();
        ctx.strokeStyle = '#2c3e50';
        ctx.lineWidth = 2;
        ctx.stroke();
        
        // Draw status indicator
        ctx.beginPath();
        ctx.arc(x, y, size - 15, 0, 2 * Math.PI);
        ctx.fillStyle = isHealthy ? '#27ae60' : '#e74c3c';
        ctx.fill();
        
        // Draw label
        ctx.fillStyle = '#2c3e50';
        ctx.font = 'bold 12px Arial';
        ctx.textAlign = 'center';
        ctx.fillText(label, x, y + size + 20);
        
        // Draw status text
        if (status) {
            ctx.font = '10px Arial';
            ctx.fillText(status.status, x, y + size + 35);
        }
    }
    
    drawConnection(ctx, x1, y1, x2, y2, status1, status2) {
        const isHealthy = status1 && status1.status === 'healthy' && status2 && status2.status === 'healthy';
        
        ctx.beginPath();
        ctx.moveTo(x1, y1);
        ctx.lineTo(x2, y2);
        ctx.strokeStyle = isHealthy ? '#27ae60' : '#e74c3c';
        ctx.lineWidth = 3;
        ctx.stroke();
        
        // Draw arrow
        const angle = Math.atan2(y2 - y1, x2 - x1);
        const arrowLength = 10;
        const arrowX = x2 - arrowLength * Math.cos(angle);
        const arrowY = y2 - arrowLength * Math.sin(angle);
        
        ctx.beginPath();
        ctx.moveTo(x2, y2);
        ctx.lineTo(arrowX - 5 * Math.sin(angle), arrowY + 5 * Math.cos(angle));
        ctx.moveTo(x2, y2);
        ctx.lineTo(arrowX + 5 * Math.sin(angle), arrowY - 5 * Math.cos(angle));
        ctx.stroke();
    }
    
    drawLegend(ctx, x, y) {
        ctx.fillStyle = '#2c3e50';
        ctx.font = 'bold 14px Arial';
        ctx.textAlign = 'left';
        ctx.fillText('Legend:', x, y);
        
        const legendItems = [
            { color: '#27ae60', text: 'Healthy' },
            { color: '#e74c3c', text: 'Unhealthy' },
            { color: '#3498db', text: 'Coordinator' },
            { color: '#27ae60', text: 'Worker' }
        ];
        
        legendItems.forEach((item, index) => {
            ctx.fillStyle = item.color;
            ctx.beginPath();
            ctx.arc(x + 20, y + 25 + index * 20, 5, 0, 2 * Math.PI);
            ctx.fill();
            
            ctx.fillStyle = '#2c3e50';
            ctx.font = '12px Arial';
            ctx.fillText(item.text, x + 35, y + 30 + index * 20);
        });
    }
    
    updateComponentList() {
        const componentList = document.getElementById('component-list');
        componentList.innerHTML = '';
        
        if (!this.data || !this.data.system_status) return;
        
        Object.values(this.data.system_status.components).forEach(component => {
            const div = document.createElement('div');
            div.className = `component-item ${component.status}`;
            
            div.innerHTML = `
                <div class="component-name">${component.id.toUpperCase()}</div>
                <div class="component-stats">
                    Status: ${component.status} | 
                    CPU: ${component.cpu_usage.toFixed(1)}% | 
                    Memory: ${component.memory_usage.toFixed(1)}MB | 
                    Connections: ${component.active_connections}
                </div>
            `;
            
            componentList.appendChild(div);
        });
    }
    
    drawQueryFlow() {
        const canvas = this.canvases.queryFlow;
        const ctx = canvas.ctx;
        const width = canvas.width;
        const height = canvas.height;
        
        // Clear canvas
        ctx.clearRect(0, 0, width, height);
        
        // Draw query execution steps
        const steps = [
            { name: 'SQL Parser', x: 100, y: 100, status: 'completed' },
            { name: 'Query Planner', x: 300, y: 100, status: 'completed' },
            { name: 'Shard Assignment', x: 500, y: 100, status: 'completed' },
            { name: 'Worker Execution', x: 300, y: 250, status: 'running' },
            { name: 'Result Aggregation', x: 500, y: 250, status: 'pending' }
        ];
        
        steps.forEach((step, index) => {
            this.drawQueryStep(ctx, step.x, step.y, step.name, step.status);
            
            // Draw arrows between steps
            if (index < steps.length - 1) {
                const nextStep = steps[index + 1];
                this.drawArrow(ctx, step.x + 60, step.y + 20, nextStep.x - 10, nextStep.y + 20);
            }
        });
        
        // Draw title
        ctx.fillStyle = '#2c3e50';
        ctx.font = 'bold 20px Arial';
        ctx.textAlign = 'center';
        ctx.fillText('Query Execution Flow', width / 2, 40);
    }
    
    drawQueryStep(ctx, x, y, name, status) {
        const width = 120;
        const height = 40;
        
        // Draw step rectangle
        ctx.fillStyle = this.getStatusColor(status);
        ctx.fillRect(x, y, width, height);
        
        // Draw border
        ctx.strokeStyle = '#2c3e50';
        ctx.lineWidth = 2;
        ctx.strokeRect(x, y, width, height);
        
        // Draw text
        ctx.fillStyle = '#2c3e50';
        ctx.font = 'bold 12px Arial';
        ctx.textAlign = 'center';
        ctx.fillText(name, x + width / 2, y + height / 2 + 4);
        
        // Draw status indicator
        ctx.fillStyle = this.getStatusDotColor(status);
        ctx.beginPath();
        ctx.arc(x + width - 15, y + 15, 5, 0, 2 * Math.PI);
        ctx.fill();
    }
    
    drawArrow(ctx, x1, y1, x2, y2) {
        ctx.beginPath();
        ctx.moveTo(x1, y1);
        ctx.lineTo(x2, y2);
        ctx.strokeStyle = '#2c3e50';
        ctx.lineWidth = 2;
        ctx.stroke();
        
        // Draw arrowhead
        const angle = Math.atan2(y2 - y1, x2 - x1);
        const arrowLength = 10;
        const arrowX = x2 - arrowLength * Math.cos(angle);
        const arrowY = y2 - arrowLength * Math.sin(angle);
        
        ctx.beginPath();
        ctx.moveTo(x2, y2);
        ctx.lineTo(arrowX - 5 * Math.sin(angle), arrowY + 5 * Math.cos(angle));
        ctx.moveTo(x2, y2);
        ctx.lineTo(arrowX + 5 * Math.sin(angle), arrowY - 5 * Math.cos(angle));
        ctx.stroke();
    }
    
    updatePerformanceMetrics() {
        if (!this.data || !this.data.performance_metrics) return;
        
        const metrics = this.data.performance_metrics;
        
        document.getElementById('total-queries').textContent = metrics.total_queries || 0;
        document.getElementById('avg-latency').textContent = `${(metrics.average_latency_ms || 0).toFixed(1)}ms`;
        document.getElementById('qps').textContent = (metrics.queries_per_second || 0).toFixed(1);
        document.getElementById('error-rate').textContent = `${((metrics.error_rate || 0) * 100).toFixed(1)}%`;
        
        // Update worker utilization charts
        this.updateWorkerCharts(metrics.worker_utilization || {});
    }
    
    updateWorkerCharts(utilization) {
        const chartsContainer = document.getElementById('worker-charts');
        chartsContainer.innerHTML = '';
        
        Object.entries(utilization).forEach(([workerId, util]) => {
            const chartDiv = document.createElement('div');
            chartDiv.className = 'worker-chart';
            
            chartDiv.innerHTML = `
                <h4>${workerId.toUpperCase()}</h4>
                <div class="utilization-bar">
                    <div class="utilization-fill" style="width: ${util}%"></div>
                </div>
                <div>${util.toFixed(1)}%</div>
            `;
            
            chartsContainer.appendChild(chartDiv);
        });
    }
    
    drawDemo() {
        const canvas = this.canvases.demo;
        const ctx = canvas.ctx;
        const width = canvas.width;
        const height = canvas.height;
        
        // Clear canvas
        ctx.clearRect(0, 0, width, height);
        
        // Draw demo animation
        const time = Date.now() * 0.001;
        
        // Draw pulsing circles representing data flow
        for (let i = 0; i < 3; i++) {
            const x = 100 + i * 200;
            const y = height / 2;
            const radius = 30 + Math.sin(time + i) * 10;
            
            ctx.beginPath();
            ctx.arc(x, y, radius, 0, 2 * Math.PI);
            ctx.fillStyle = `rgba(52, 152, 219, ${0.3 + Math.sin(time + i) * 0.2})`;
            ctx.fill();
            ctx.strokeStyle = '#3498db';
            ctx.lineWidth = 2;
            ctx.stroke();
        }
        
        // Draw title
        ctx.fillStyle = '#2c3e50';
        ctx.font = 'bold 18px Arial';
        ctx.textAlign = 'center';
        ctx.fillText('Interactive Demo - Data Flow Visualization', width / 2, 40);
    }
    
    executeQuery() {
        const query = document.getElementById('query-input').value;
        if (!query.trim()) return;
        
        // Show loading state
        const executeBtn = document.getElementById('execute-query');
        executeBtn.textContent = 'Executing...';
        executeBtn.disabled = true;
        
        // Execute query via API
        fetch('/api/query', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ query: query })
        })
        .then(response => response.json())
        .then(data => {
            this.displayQueryResults(data);
        })
        .catch(error => {
            console.error('Query execution error:', error);
            this.displayQueryResults({
                error: 'Failed to execute query',
                results: []
            });
        })
        .finally(() => {
            executeBtn.textContent = 'Execute Query';
            executeBtn.disabled = false;
        });
    }
    
    displayQueryResults(data) {
        const resultsContainer = document.getElementById('query-results');
        
        if (data.error) {
            resultsContainer.innerHTML = `<div style="color: #e74c3c;">Error: ${data.error}</div>`;
            return;
        }
        
        if (!data.results || data.results.length === 0) {
            resultsContainer.innerHTML = '<div>No results returned</div>';
            return;
        }
        
        let html = `
            <div style="margin-bottom: 10px;">
                <strong>Query:</strong> ${data.sql_query}<br>
                <strong>Execution Time:</strong> ${data.execution_time_ms}ms<br>
                <strong>Rows Returned:</strong> ${data.rows_returned}
            </div>
            <table style="width: 100%; border-collapse: collapse;">
        `;
        
        data.results.forEach((row, index) => {
            html += '<tr>';
            row.forEach(cell => {
                html += `<td style="border: 1px solid #ddd; padding: 8px;">${cell}</td>`;
            });
            html += '</tr>';
        });
        
        html += '</table>';
        resultsContainer.innerHTML = html;
    }
    
    startDemo() {
        document.getElementById('demo-info-content').innerHTML = 'Demo started! Watch the data flow visualization above.';
    }
    
    stopDemo() {
        document.getElementById('demo-info-content').innerHTML = 'Demo stopped. Click "Start Demo" to begin again.';
    }
    
    getComponentStatus(componentId) {
        if (!this.data || !this.data.system_status || !this.data.system_status.components) {
            return null;
        }
        return this.data.system_status.components[componentId] || null;
    }
    
    getStatusColor(status) {
        switch (status) {
            case 'completed': return '#27ae60';
            case 'running': return '#f39c12';
            case 'pending': return '#3498db';
            case 'failed': return '#e74c3c';
            default: return '#bdc3c7';
        }
    }
    
    getStatusDotColor(status) {
        switch (status) {
            case 'completed': return '#27ae60';
            case 'running': return '#f39c12';
            case 'pending': return '#3498db';
            case 'failed': return '#e74c3c';
            default: return '#bdc3c7';
        }
    }
}

// Initialize the application when the page loads
document.addEventListener('DOMContentLoaded', () => {
    new VisualizerApp();
});
