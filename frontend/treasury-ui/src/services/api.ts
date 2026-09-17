import axios from 'axios';
import type {CreateTradePayload, Trade} from '../types';
import { TRADER_TOKEN } from '../utils/tokens';

// Route via API Gateway port 8080
const API_GATEWAY_URL = 'http://localhost:8080';

const apiClient = axios.create({
    baseURL: API_GATEWAY_URL,
});

// Attach Trader token for trade bookings
apiClient.interceptors.request.use((config) => {
    config.headers.Authorization = `Bearer ${TRADER_TOKEN}`;
    return config;
});

export const fetchInitialTrades = async (): Promise<Trade[]> => {
    const response = await apiClient.get<Trade[]>('/api/v1/trades');
    return response.data;
};

export const submitTrade = async (payload: CreateTradePayload): Promise<Trade> => {
    const response = await apiClient.post<Trade>('/api/v1/trades', payload);
    return response.data;
};