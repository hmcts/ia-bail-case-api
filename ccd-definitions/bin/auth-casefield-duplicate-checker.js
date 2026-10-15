const fs = require('fs');
const path = require('path');

const fileName = path.join(__dirname, '../json/AuthorisationCaseField.json');

try {
  handleContent(fs.readFileSync(fileName, 'utf8'));
} catch (err) {
  console.error(err);
  process.exit(1);
}

function handleContent(content) {
  const caseEventToFieldJson = JSON.parse(content);

  const keyCountMap = new Map();
  const duplicates = new Set();

  caseEventToFieldJson.forEach(line => {
    const uniqueKey = line.CaseFieldID.concat('+').concat(line.UserRole);
    if (keyCountMap.has(uniqueKey)) {
      keyCountMap.set(uniqueKey, keyCountMap.get(uniqueKey) + 1);
      duplicates.add(uniqueKey);
    } else {
      keyCountMap.set(uniqueKey, 1);
    }
  });

  if (duplicates.size) {
    console.log('\n\rCaseFieldID-UserRole row duplications: ')
    duplicates.forEach(duplicate => {
      const parts = duplicate.split('+');
      const caseFieldID = parts[0];
      const userRole = parts[1];
      console.log('CaseFieldID: ' + caseFieldID + ', UserRole: ' + userRole)
    });
    process.exit(1);
  }
}
