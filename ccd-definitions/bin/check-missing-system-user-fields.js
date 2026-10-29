const { readSheet } = require('./lib/definition');

// Field types that hold no case data, so a system-user grant on them means nothing.
const NON_DATA_FIELD_TYPES = ['Label', 'CaseHistoryViewer'];

try {
    handleContent(readSheet('CaseField'), readSheet('AuthorisationCaseField'));
} catch (error) {
    console.error('Error reading files:', error);
    process.exit(1);
}

function handleContent(caseFields, Authorisations) {

    const caseFieldIds = caseFields
      .filter(entry => !NON_DATA_FIELD_TYPES.includes(entry.FieldType))
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
