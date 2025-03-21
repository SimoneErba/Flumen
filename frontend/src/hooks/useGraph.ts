import { useState, useEffect } from 'react';
import { useApi } from './useApi';
import { useWebSocket, PositionUpdate, NodeUpdate } from './useWebSocket';
import { GraphData as ApiGraphData, GraphData } from '../api-client';


const convertApiGraphData = (data: ApiGraphData): GraphData => {
    return {
        nodes: data.nodes?.map(node => ({
            id: node.id || '',
            type: (node.type as 'item' | 'location') || 'item',
            properties: node.properties || {}
        })) || [],
        edges: data.edges?.map(edge => ({
            id: edge.id || '',
            source: edge.source || '',
            target: edge.target || '',
            type: 'position',
            properties: edge.properties || {}
        })) || []
    };
};

export const useGraph = () => {
    const { graphApi } = useApi();
    const { connected, subscribeToPositionUpdates, subscribeToNodeUpdates } = useWebSocket();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [graphData, setGraphData] = useState<GraphData>(emptyGraphData);

    // Fetch initial graph data
    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                const response = await graphApi.getGraphData();
                setGraphData(convertApiGraphData(response.data));
                setError(null);
            } catch (err) {
                setError('Failed to fetch graph data');
                console.error('Error fetching graph data:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [graphApi]);

    // Handle position updates
    useEffect(() => {
        if (!connected) return;

        const handlePositionUpdate = (update: PositionUpdate) => {
            setGraphData(current => {
                const newEdges = current.edges.filter(edge => 
                    edge.source !== update.itemId
                );

                if (update.locationId) {
                    newEdges.push({
                        id: `${update.itemId}-${update.locationId}`,
                        source: update.itemId,
                        target: update.locationId,
                        type: 'position',
                        properties: {}
                    });
                }

                return {
                    ...current,
                    edges: newEdges
                };
            });
        };

        const unsubscribe = subscribeToPositionUpdates(handlePositionUpdate);
        return () => unsubscribe();
    }, [connected, subscribeToPositionUpdates]);

    // Handle node updates
    useEffect(() => {
        if (!connected) return;

        const handleNodeUpdate = (update: NodeUpdate) => {
            setGraphData(current => {
                const newNodes = current.nodes.map(node =>
                    node.id === update.id
                        ? { ...node, properties: { ...node.properties, ...update.properties } }
                        : node
                );

                return {
                    ...current,
                    nodes: newNodes
                };
            });
        };

        const unsubscribe = subscribeToNodeUpdates(handleNodeUpdate);
        return () => unsubscribe();
    }, [connected, subscribeToNodeUpdates]);

    return {
        graphData,
        loading,
        error,
        connected
    };
}; 