package com.jakewharton.sdkmanager.internal

import org.junit.Test

import static com.jakewharton.sdkmanager.internal.PackageResolver.fulfillsDependency
import static org.fest.assertions.api.Assertions.assertThat

class PackageResolverTest {
  @Test public void fulfillsDependencyExamples() {
    def version = [19, 1, 0] as int[]
    assertThat(fulfillsDependency('18.0.0', version)).isTrue()
    assertThat(fulfillsDependency('18.0.+', version)).isTrue()
    assertThat(fulfillsDependency('18.+', version)).isTrue()
    assertThat(fulfillsDependency('19.1.0', version)).isTrue()
    assertThat(fulfillsDependency('19.1.+', version)).isTrue()
    assertThat(fulfillsDependency('19.+', version)).isTrue()
    assertThat(fulfillsDependency('+', version)).isTrue()
    assertThat(fulfillsDependency('19.1.1', version)).isFalse()
    assertThat(fulfillsDependency('19.2.0', version)).isFalse()
    assertThat(fulfillsDependency('20.1.0', version)).isFalse()
  }
}
