import { test as baseTest } from "@playwright/test";
import { PageFixtures, pageFixtures } from "./page-objects";
import { UtilsFixtures, utilsFixtures } from "./utils";

export type CustomFixtures = PageFixtures & UtilsFixtures;

export const test = baseTest.extend<CustomFixtures>({
  ...pageFixtures,
  ...utilsFixtures,
});

export const expect = test.expect;
