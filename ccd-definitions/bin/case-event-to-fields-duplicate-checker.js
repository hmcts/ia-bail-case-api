const fs = require('fs');
const path = require('path');

const fileName = path.join(__dirname, '../json/CaseEventToFields.json');

try {
  checkContent(fs.readFileSync(fileName, 'utf8'));
} catch (err) {
  console.error(err);
  process.exit(1);
}

function checkContent(content) {
  const caseEventToFieldJson = JSON.parse(content);

  const keyCountMap = new Map();
  const duplicates = new Set();

  caseEventToFieldJson.forEach(line => {
    const uniqueKey = line.CaseEventID.concat('-').concat(line.CaseFieldID);

    if (keyCountMap.has(uniqueKey)) {
      keyCountMap.set(uniqueKey, keyCountMap.get(uniqueKey) + 1);
      duplicates.add(uniqueKey);
    } else {
      keyCountMap.set(uniqueKey, 1);
    }
  });

  if (duplicates.size) {
    console.log('\n\rCaseEvent-Casefield row duplications: ')
    duplicates.forEach(duplicate => {
      const parts = duplicate.split('-');
      const caseEventID = parts[0];
      const caseFieldID = parts[1];
      console.log('CaseEventID: ' + caseEventID + ', CaseFieldID: ' + caseFieldID)
    });
    process.exit(1);
  }
}
