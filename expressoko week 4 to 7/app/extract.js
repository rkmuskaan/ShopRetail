const fs = require('fs');
const content = fs.readFileSync('app/src/main/java/com/example/ui/screens/ECommerceUI.kt', 'utf8');
const matches = content.match(/"[^"]+"/g) || [];
const un = [...new Set(matches)];
fs.writeFileSync('strings.txt', un.join('\n'));
