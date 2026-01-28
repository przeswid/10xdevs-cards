// Quick test to verify the API client calls the backend
import { apiClient } from './src/lib/api/client.ts';

console.log('Testing API call to backend...');
console.log('Backend URL:', process.env.PUBLIC_API_URL || 'http://localhost:8080');

apiClient.get('/flashcards')
  .then(response => {
    console.log('\n✅ SUCCESS: API call to /flashcards worked!');
    console.log('Total flashcards:', response.data.page.totalElements);
    console.log('Flashcards:', JSON.stringify(response.data.content, null, 2));
  })
  .catch(error => {
    console.error('\n❌ ERROR:', error.message);
    if (error.response) {
      console.error('Status:', error.response.status);
      console.error('Data:', error.response.data);
    }
  });
