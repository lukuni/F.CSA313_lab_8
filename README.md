
# Лаб 08 — Mock объектууд (Mockito)

**F.CSA313 — Программ хангамжийн чанарын баталгаа ба тест (2026)**

- **Оюутны нэр:** Ч.Оюумаа
- **Оюутны код:** B232270133

## Орчин

```
$ java -version
openjdk version "27" 2026-09-15
OpenJDK Runtime Environment Homebrew (build 27)
OpenJDK 64-Bit Server VM Homebrew (build 27, mixed mode, sharing)

$ mvn -version
Apache Maven 3.9.16 (2bdd9fddda4b155ebf8000e807eb73fd829a51d5)
Maven home: /opt/homebrew/Cellar/maven/3.9.16/libexec
Java version: 27, vendor: Homebrew, runtime: /opt/homebrew/Cellar/openjdk/27/libexec/openjdk.jdk/Contents/Home
Default locale: en_MN, platform encoding: UTF-8
OS name: "mac os x", version: "26.1", arch: "aarch64", family: "mac"
```

## Репозиторийн бүтэц

| Файл | Зориулалт |
|---|---|
| `pom.xml` | JUnit 5 + Mockito 5 тохиргоо (багшийн өгсөн) |
| `src/main/java/mn/edu/must/sqat/` | `Student`, `StudentRepository`, `EmailSender`, `CourseRegistrationService` (тестлэгдэх класс, багшийн өгсөн) |
| `src/test/java/mn/edu/must/sqat/CourseRegistrationServiceTest.java` | Хэсэг A-ийн 8 тест (A1–A8) |
| `results/mvn-test.txt` | Эх кодын ногоон гаралт (10 тест, BUILD SUCCESS) |
| `results/mvn-test-mutant-1.txt` | Мутант 1-ийн гаралт (BUILD FAILURE) |
| `results/mvn-test-mutant-2.txt` | Мутант 2-ын гаралт (BUILD FAILURE) |
| `results/mvn-test-mutant-3.txt` | Мутант 3-ын гаралт, A6 засварласны дараа (BUILD FAILURE) |

---

## Хэсэг A — Найман тест

`CourseRegistrationService` нь `StudentRepository` ба `EmailSender` гэсэн хоёр хамааралтай. Хоёуланг нь `@Mock`-оор орлуулж, сан, SMTP сервергүйгээр бизнес логикийг тусгаарлан тестэлсэн.

| № | Тестийн нэр | Хүлээгдэх үр дүн | Гол хэрэгсэл |
|---|---|---|---|
| A1 | `register_activeStudentWithPrereqs_returnsOkSavesAndSendsEmail` | `OK`, `saveRegistration` дуудагдаж, и-мэйл явна | `when` + `verify` |
| A2 | `register_unknownStudent_returnsNoStudentAndDoesNotSaveOrEmail` | `ERROR_NO_STUDENT`, хадгалаагүй, и-мэйл яваагүй | `verify(..., never())` |
| A3 | `register_inactiveStudent_returnsInactiveAndNeverTouchesEmail` | `ERROR_INACTIVE_STUDENT`, и-мэйлтэй харьцаагүй | `verifyNoInteractions` |
| A4 | `register_missingPrerequisites_returnsPrerequisitesAndDoesNotSave` | `ERROR_PREREQUISITES`, хадгалаагүй | `when` + `never` |
| A5 | `register_emailSenderThrows_stillReturnsOkAndSaved` | и-мэйл exception шидсэн ч `OK`, хадгалагдсан | `doThrow(...).when(...)` |
| A6 | `register_nullArgument_throwsIllegalArgumentAndTouchesNothing` | `IllegalArgumentException`, хамаарлуудтай харьцаагүй | `assertThrows` |
| A7 | `register_success_emailBodyContainsCourseCode` | и-мэйлийн биед `CS201` орсон | `ArgumentCaptor` |
| A8 | `register_errorCases_returnExpectedCode` (3 мөр) | 3 алдааны ангийн код | `@ParameterizedTest` + `@CsvSource` |

Тестийн нэрс `юуг_ямарНөхцөлд_юуХүлээгдэнэ` хэв маягтай. Нэг дуудалтад matcher хэрэглэсэн бол бүх аргументад matcher хэрэглэсэн (`eq`, `anyString`).

> Тестийн тоо **10** = 7 энгийн тест + A8-ийн 3 мөр = `results/mvn-test.txt`-ийн сүүлийн мөр: `Tests run: 10, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`.

### A8 дахь `lenient()`

A8-д мөр бүр өөр stub ашигладаг: жишээ нь оюутан олдоогүй мөрөнд `courseExists`, `prerequisites` stub огт дуудагдахгүй. `MockitoExtension`-ий strict stubs горим ашиглагдаагүй stub-д `UnnecessaryStubbingException` өгдөг тул эдгээр stub дээр `lenient().when(...)` ашигласан.

---

## Хэсэг B — Мини мутаци

Тест үнэхээр ажил хийж байгааг батлахын тулд `CourseRegistrationService.java`-г зориуд эвдэж, тест унаж байгааг шалгасан. Мутант бүрийн дараа `git checkout src/main`-аар эх кодыг сэргээсэн.

| № | Эвдрэл | Унасан тест | Төрөл | Файл |
|---|---|---|---|---|
| 1 | `if (!student.active()) return ...` мөрийг комментлосон | `register_inactiveStudent_returnsInactiveAndNeverTouchesEmail` (A3), `register_errorCases_returnExpectedCode[2]` (A8) | Failure (assertEquals) | `results/mvn-test-mutant-1.txt` |
| 2 | `repository.saveRegistration(...)` дуудалтыг комментлосон | `register_activeStudentWithPrereqs_returnsOkSavesAndSendsEmail` (A1), `register_emailSenderThrows_stillReturnsOkAndSaved` (A5) | Failure (verify: Wanted but not invoked) | `results/mvn-test-mutant-2.txt` |
| 3 | `if (studentId == null \|\| courseId == null)`-аас `\|\| courseId == null`-г устгасан | `register_nullArgument_throwsIllegalArgumentAndTouchesNothing` (A6, засварласны дараа) | Failure (assertThrows) | `results/mvn-test-mutant-3.txt` |

Гурван мутантад `BUILD FAILURE` гарсан. `UnnecessaryStubbingException` гараагүй, бүгд жирийн `Failures`.

**Мутант 1:** A3 `ERROR_INACTIVE_STUDENT` хүлээсэн боловч `ERROR_NO_COURSE` авсан (`courseExists` stub-гүй тул mock `false` буцаасан). A8-ийн 2-р мөр `OK` авсан.

**Мутант 2:** A1 ба A5 хоёулаа `repository.saveRegistration("S1", "CS201")` дуудагдаагүй гэж `verify`-ээр унасан.

### Мутант 3 — тестийн цоорхой

Анхны A6 зөвхөн `register(null, "CS201")`-г шалгадаг байсан тул мутант 3 **амьд үлдсэн**: `Tests run: 10, Failures: 0`, `BUILD SUCCESS`. Тест `courseId == null` тохиолдлыг огт хамаардаггүй байсан нь тестийн цоорхой байв.

**Засвар:** A6-г `register("S1", null)`-г мөн шалгадаг болгож, нэрийг `register_nullArgument_...` болгон өөрчилсөн (шинэ тест нэмээгүй тул тестийн тоо 10 хэвээр):

```java
assertThrows(IllegalArgumentException.class, () -> service.register(null, "CS201"));
assertThrows(IllegalArgumentException.class, () -> service.register("S1", null));
verifyNoInteractions(repository, emailSender);
```

Үүний дараа мутант 3 унасан: мутанттай үед `register("S1", null)` exception шидэхгүй, `repository.findStudent("S1")` дуудаж mock-оос `null` авч `ERROR_NO_STUDENT` буцаадаг тул `assertThrows` "Expected IllegalArgumentException to be thrown, but nothing was thrown" гэж унасан. Эцэст нь эх кодыг сэргээж `results/mvn-test.txt`-ийг дахин ажиллуулахад 10 тест ногоон болсон.

---

## Дүгнэлт

Mock нь тест дотроо тохируулагдаж, `verify`-аар ямар хамаарал хэзээ дуудагдсаныг шалгах боломж өгдөг бол гараар бичсэн fake нь жинхэнээр ажилладаг хөнгөн хэрэгжүүлэлт (жишээ нь санах ойн repository) бөгөөд төлөв шалгахад тохиромжтой. Fake-ийг өөрөө хөтөлж, интерфейс өөрчлөгдөх бүрд засах шаардлагатай, харин mock бага кодоор тохируулагддаг. Тестлэж буй классаа болон `Student` шиг энгийн утгын объектыг mock хийх ёсгүй, учир нь тэгвэл тест бодит зан төлөвийг биш mock-ийн тохиргоог шалгана. Өөрийн эзэмшдэггүй гуравдагч талын төрлийг шууд mock хийхээс зайлсхийж, өөрийн интерфейсээр орлуулах нь дээр. `verify`-г хэтрүүлбэл, жишээ нь бүх дуудалт, тэдгээрийн дарааллыг шалгавал тест хэрэгжүүлэлтэд наалдаж, зан төлөв өөрчлөгдөөгүй энгийн refactoring-д ч унана. Мутант 3 нь бүх тест ногоон байсан ч хамрах хүрээ дутуу байж болохыг харуулсан: анхны A6 зөвхөн нэг null аргументыг шалгадаг байв. Мутант 1-д A3 нь `ERROR_NO_COURSE` авч унасан нь mock-ийн stub-гүй дуудалт default утга (`false`) буцаадгийг харуулсан. Тиймээс кодыг зориуд эвдэж тестээ шалгах нь тестийн үнэ цэнийг батлах энгийн бөгөөд үр дүнтэй арга гэдгийг ойлгов.
