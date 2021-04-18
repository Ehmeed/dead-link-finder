#!/usr/bin/env python3

from flask import Flask
from flask import redirect

HOST = "localhost"
PORT = 5000

app = Flask(__name__)


@app.route("/")
def homepage():
    return """
    <p>Hello</p>
    <p>world</p>
    """


@app.route('/redirect')
def redirect_example():
    return redirect('/')


@app.route('/same-domain')
def same_domain():
    return f"""
         <a href="http://www.{HOST}:{PORT}">LINK</a>
    """


@app.route('/depth')
def depth():
    return f"""
         <a href="http://www.{HOST}:{PORT}/same-domain">LINK</a>
    """


@app.route('/multiple')
def multiple():
    return f"""
         <a href="http://www.{HOST}:{PORT}">LINK</a>
         <a href="http://www.{HOST}:{PORT}/redirect">LINK</a>
    """


@app.route('/multiple-same')
def multiple_same():
    return f"""
         <a href="http://www.{HOST}:{PORT}">LINK</a>
         <a href="http://www.{HOST}:{PORT}">LINK</a>
    """


@app.route('/cross-domain')
def cross_domain():
    return """
         <a href="https://www.example.com">LINK</a>
    """


@app.route('/root-relative')
def root_relative():
    return """
         <a href="/empty">LINK</a>
    """


@app.route('/relative-dead')
def relative_dead():
    return """
         <a href="dead">LINK</a>
    """

@app.route('/anchor')
def anchor():
    return f"""
         <a href="#lmao">LINK</a>
    """

@app.route('/empty')
def empty():
    return ""


@app.route('/forbidden')
def login():
    return "no", 403


if __name__ == '__main__':
    app.run(debug=True, use_reloader=True)
