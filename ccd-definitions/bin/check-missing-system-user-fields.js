const fs = require('fs');
const path = require('path');

const caseFieldPath = path.join(__dirname, '../json/CaseField.json');
const authorisationCaseFieldPath = path.join(__dirname, '../json/AuthorisationCaseField.json');

try {
    const caseField = fs.readFileSync(caseFieldPath, 'utf8');
    const authCaseField = fs.readFileSync(authorisationCaseFieldPath, 'utf8');
    handleContent(caseField, authCaseField);
} catch (error) {
    console.error('Error reading files:', error);
    process.exit(1);
}

function handleContent(content, contentAuth) {
    const caseFields = JSON.parse(content)
    const Authorisations = JSON.parse(contentAuth)

    const caseFieldIds = caseFields
      .filter(entry => entry.FieldType !== 'Label')
      .map(entry => entry.ID)
      .filter(id => !id.includes('progress_') ); // Ignore IDs with 'progress_'

    const AuthorisationsEntries = Authorisations.filter(entry =>
      entry.UserRole === 'caseworker-ia-system' && entry.CRUD === 'CRUD'
    );
    const AuthorisationsIds = AuthorisationsEntries.map(entry => entry.CaseFieldID);

    const missingIDs = caseFieldIds.filter(id => !AuthorisationsIds.includes(id));

    if (missingIDs.length > 0) {
        console.log('The following relevant IDs are missing in the second file with the specified UserRole and CRUD:', missingIDs);
        console.log('Add the following authorisations to AuthorisationCaseField file');
        missingIDs.forEach(element => {
            console.log("{\"LiveFrom\": \"01/01/2018\", \"CaseTypeID\": \"Bail\", \"CaseFieldID\": \"" + element + "\", \"UserRole\": \"caseworker-ia-system\", \"CRUD\": \"CRUD\"},");
        });
        process.exit(1);
    }
}
