# Example form using React/Helix

My own response to and solution for the problems described in
- ["What makes frontend development tricky" by Abhinav Omprakash](https://www.abhinavomprakash.com/posts/what-makes-frontend-development-tricky/)
- [Christian Johansen's followup](https://cjohansen.no/stateless-data-driven-uis/)

# Overview

This repository shows how to implement the example app described in the article
using plain React. The implementation aims to encapsulate behavior, styling
and structure in components in a composable and testable way by using
fundamental principles of React component design, while also meeting the
business requirements of the application.

The implementation is only 142 lines of code. It is accompanied by several tests
that ensure that the business logic is correctly implemented and the web page is
updated correctly according to the business requirements.

## Business requirements

The application consists of a form which accepts 3 values:
- A singe text field which must match the format DD-MM-YYYY
- Two text fields, similar to above, with the added constraint that they must be
  in ascending order (i.e. a "start" field and an "end" field)
  
Their should be a submit button that clears the form on click.

If any of the text fields are malformed, then an error message should be shown
directly below the invalid one explaining that the value isn't formatted
correctly. The text field should also have a red border.

If the two text fields are not in ascending order, then an error should be shown
beneath both text fields explaining the issue. Both text fields should have a
red border in this case.

If any fields are invalid, then the submit button should be disabled and should
not do anything when clicked until all fields are valid again.

Empty values are considered valid.

## Implementation notes

Based on the requirements, a few things fall out:

- A single component, `date-field`, can be used to encapsulate the structure,
  style, and date validation logic of all 3 text fields
- In order to check the range is correct, we need access to both the start and
  end dates, so we can encapsulate this in a `date-range` component which
  controls the state of both `date-field`s
- However, because we need the state of all 3 fields to discern whether to
  enable the submit button and clear the state on successful click, it makes
  sense to control the state of the fields in the parent component of all 3
  fields and the submit button.

The main tension in this problem is how to create encapsulated, reusable
components while also accommodating cross-field validation.

The implementation takes the following approach:

0. `date-field` and `date-range` components implement the style and structure to
   display the right UI based on props passed to it. They have no local state.
1. Validation logic is handled locally within these components.
   1. `date-field` contains the logic for checking the date format and showing
      an error message
   2. `date-range` contains the logic for checking that the start and end dates
      are in ascending order
   3. Each component reports its `valid?` state via its `on-change` handler so
      that parent components can track it as needed.
2. The state of the fields is maintained in the top-level `main-panel` component
   so that the `submit-button` component can clear it on press. These are passed
   via props.

By following this approach, we achieve the following:

1. Changing the validation logic of `date-field` can be done in one place and
   affects the whole app
2. Because validation logic always happens in event handlers, we never end up in
   situations where we render, calculate some new state and then re-render again
3. Maintaining all the state in `main-panel` doesn't make it any less testable;
   it does mean that we can focus on just testing the cross-field validation
   concerns, i.e. ensuring the submit button is enabled/disabled appopriately.

Because all logic, behavior, structure and styling is encapsulated inside of
components, we can test these components using [react-testing-library](https://testing-library.com/docs/react-testing-library/intro/).

This allows us to test the combination of all of these concerns in a wholistic
way, but focused on execution of the specific feature we are building.

In a more complex app, this can be very beneficial as it avoids having to
execute the application end to end to test these things wholistically.

# Thoughts on forms

Forms are particularly tricky to do in React because the often require very
tricky UX that isn't served well by one-way databinding, and they can also be
large and nested. They also often require integration with things like network
I/O, navigation, and state that React is good at.

Because of the particular complexity of handling forms interactions and
nestedness, a purpose-built tool can be beneficial. At work, I have experience
with [react-hook-form](https://react-hook-form.com/). There are many other
libraries in this space, and I expect that new solutions will continue to be
developed. Adopting one of these libraries often is better than rolling your own
framework for handling arbitrary form state.

# Developing

Initial setup:

```shellsession
$ npm i
```

To start the development server and build the code,

```shellsession
$ npx shadow-cljs watch app test
```

Open http://localhost:8700 to see the app, and http://localhost:8021 to run the
tests in your browser.
