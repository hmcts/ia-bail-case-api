const { readSheet } = require('./lib/definition');

try {
  checkContent(readSheet('CaseEventToFields'));
} catch (err) {
  console.error(err);
  process.exit(1);
}

function checkContent(caseEventToFieldJson) {

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
