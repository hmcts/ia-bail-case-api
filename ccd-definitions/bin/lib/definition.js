const fs = require('fs');
const path = require('path');

const DEFINITION_DIR = path.join(__dirname, '../../../build/ccd-definitions-json');

// A sheet is either <Sheet>.json or a <Sheet>/ directory the SDK splits the larger sheets into.
function readSheet(sheet) {
  const file = path.join(DEFINITION_DIR, `${sheet}.json`);
  if (fs.existsSync(file)) {
    return JSON.parse(fs.readFileSync(file, 'utf8'));
  }

  const dir = path.join(DEFINITION_DIR, sheet);
  if (!fs.existsSync(dir)) {
    throw new Error(
      `No ${sheet} sheet in ${DEFINITION_DIR} - run 'yarn generate-<env>' before checking.`
    );
  }

  return fs.readdirSync(dir)
    .filter(name => name.endsWith('.json'))
    .flatMap(name => JSON.parse(fs.readFileSync(path.join(dir, name), 'utf8')));
}

module.exports = { DEFINITION_DIR, readSheet };
