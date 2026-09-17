/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                fintech: {
                    dark: '#0f172a',
                    card: '#1e293b',
                    border: '#334155',
                    profit: '#10b981',
                    loss: '#ef4444',
                    accent: '#38bdf8'
                }
            }
        },
    },
    plugins: [],
}