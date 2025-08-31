import './App.css'
import { DisplayGraph } from './components/graph'
import { useGraph } from './hooks/useGraph';

function App() {
  const { graphData, loading } = useGraph();
  return (
    loading ? <div>Loading...</div> : 
      <DisplayGraph initialGraphData={graphData} />
  );
}

export default App;