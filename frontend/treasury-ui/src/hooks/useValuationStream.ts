import { useEffect, useState } from 'react';
import type {ValuationEvent} from '../types';

// Valuation engine streaming endpoint
const STREAM_URL = 'http://localhost:8080/api/v1/stream/valuations';

export const useValuationStream = (onTick: (event: ValuationEvent) => void) => {
    const [isConnected, setIsConnected] = useState<boolean>(false);

    useEffect(() => {
        // Open persistent HTTP connection
        const eventSource = new EventSource(STREAM_URL);

        eventSource.onopen = () => {
            setIsConnected(true);
        };

        // Listen for named 'valuation-tick' events
        eventSource.addEventListener('valuation-tick', (event: MessageEvent) => {
            try {
                const parsed: ValuationEvent = JSON.parse(event.data);
                onTick(parsed);
            } catch (err) {
                console.error('Failed to parse valuation tick', err);
            }
        });

        eventSource.onerror = () => {
            setIsConnected(false);
            eventSource.close();
        };

        // Cleanup connection on unmount
        return () => {
            eventSource.close();
        };
    }, [onTick]);

    return { isConnected };
};