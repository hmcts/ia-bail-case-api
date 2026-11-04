import { test as baseTest } from "@playwright/test";
import { PageFixtures, pageFixtures } from "./page-objects";
import { CaseFixtures, UtilsFixtures, caseFixtures, utilsFixtures } from "./utils";

export type CustomFixtures = PageFixtures & UtilsFixtures & CaseFixtures;

export const test = baseTest.extend<CustomFixtures>({
  ...pageFixtures,
  ...utilsFixtures,
  ...caseFixtures,
});

export const expect = test.expect;
