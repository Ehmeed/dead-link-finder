from flask import abort, redirect, url_for
from flask import Flask

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
    return """
         <a href="http://www.localhost:5000">LINK</a>
    """


@app.route('/empty')
def empty():
    return ""


@app.route('/forbidden')
def login():
    return "no", 403


if __name__ == '__main__':
    app.run(debug=True, use_reloader=True)
