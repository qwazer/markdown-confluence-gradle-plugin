/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.github.qwazer.markdown.confluence.core.service.markdown;

/**
 *
 * @param <R> the function's return type
 * @param <P> the function's parameter type
 */
public interface F<R, P> {

    R f(P p);
}