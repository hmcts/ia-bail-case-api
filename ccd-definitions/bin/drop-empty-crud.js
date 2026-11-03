#!/usr/bin/env node

// ccd-config-generator 7.16.0 writes an AuthorisationCaseType row for every role in the generated
// UserRole enum and takes CRUD from the role's case-type permissions, which is blank for a role
// that holds no case-type grant. CRUD is a required column on the authorisation sheets, so CCD
// does not ignore such a row - it rejects the whole definition with "Couldn't find the column CRUD
// in the sheet AuthorisationCaseType". A role with no case-type grant is simply absent from
// ccd-definitions/json, so the rows are dropped here. Delete this once the SDK stops emitting
// them (hmcts/dtsse-ccd-config-generator 0d0a92ea).

const fs = require('fs');
const path = require('path');

const SHEETS = [
  'AuthorisationCaseType',
  'AuthorisationCaseField',
  'AuthorisationCaseEvent',
  'AuthorisationCaseState'
];

const root = process.argv[2];
if (!root) {
  console.error('Usage: drop-empty-crud.js <definition-json-dir>');
  process.exit(1);
}

const isAuthorisationSheet = file =>
  SHEETS.some(sheet => file === `${sheet}.json` || path.basename(path.dirname(file)) === sheet);

const jsonFiles = dir =>
  fs.readdirSync(dir, { withFileTypes: true }).flatMap(entry => {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      return jsonFiles(full);
    }
    return entry.name.endsWith('.json') ? [full] : [];
  });

let dropped = 0;
for (const file of jsonFiles(root)) {
  const relative = path.relative(root, file);
  if (!isAuthorisationSheet(relative)) {
    continue;
  }
  const rows = JSON.parse(fs.readFileSync(file, 'utf8'));
  if (!Array.isArray(rows)) {
    continue;
  }
  const kept = rows.filter(row => String(row.CRUD ?? '').trim() !== '');
  if (kept.length === rows.length) {
    continue;
  }
  dropped += rows.length - kept.length;
  fs.writeFileSync(file, `${JSON.stringify(kept, null, 2)}\n`);
}

if (dropped > 0) {
  console.log(`Dropped ${dropped} authorisation row(s) with no CRUD permissions.`);
}
